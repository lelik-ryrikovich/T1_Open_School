package ru.t1.account_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.client.CreditProcessingClient;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Payment;
import ru.t1.account_processing.entity.Transaction;
import ru.t1.account_processing.entity.enums.*;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.PaymentRepository;
import ru.t1.account_processing.repository.TransactionRepository;
import ru.t1.aop.LogDatasourceError;
import ru.t1.aop.Metric;
import ru.t1.dto.ProductRegistryInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Бизнес-сервис для обработки транзакций клиентов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final CreditProcessingClient creditProcessingClient;

    /** Карта историй транзакций: cardId → очередь временных меток транзакций. */
    private final ConcurrentHashMap<Long, Deque<Instant>> transactionHistory = new ConcurrentHashMap<>();

    /** Максимальное количество транзакций, допустимых в пределах заданного окна времени. */
    @Value("${fraud.max-transactions}")
    private int maxTransactions;

    /** Размер временного окна (в миллисекундах), в пределах которого учитываются транзакции для фрод-аналитики. */
    @Value("${fraud.time-window-ms}")
    private long timeWindowMs;

    /** Константа: количество месяцев в году. */
    private static final int MONTHS_IN_YEAR = 12;

    /**
     * Основной метод бизнес-логики обработки транзакции.
     *
     * @param accountId идентификатор счёта
     * @param cardId идентификатор карты
     * @param type тип транзакции ({@code DEPOSIT} или {@code WITHDRAW})
     * @param amount сумма транзакции
     */
    @LogDatasourceError
    @Metric
    public void processTransaction(Long accountId, Long cardId, String type, BigDecimal amount) {
        log.info("Обработка транзакции: accountId={}, cardId={}, type={}, amount={}",
                accountId, cardId, type, amount);

        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        Transaction transaction = createTransaction(accountId, cardId, transactionType, amount);

        if (isSuspicious(cardId)) {
            handleFraudulentTransaction(transaction);
            return;
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Счёт не найден: " + accountId));

        if (account.getStatus() == AccountStatus.BLOCKED || account.getStatus() == AccountStatus.ARRESTED) {
            log.warn("Счёт {} заблокирован или арестован, транзакция отклонена", account.getId());
            transaction.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(transaction);
            return;
        }

        if (Boolean.TRUE.equals(account.getIsRecalc())) {
            ProductRegistryInfo registry = creditProcessingClient.getProductRegistryByAccount(account.getId());
            int monthCount = registry != null ? registry.getMonthCount() : 60;
            createPaymentSchedule(account, amount, account.getInterestRate(), monthCount);
        }

        applyTransaction(account, transaction);
    }

    /**
     * Создаёт и сохраняет новую транзакцию в статусе {@link TransactionStatus#PROCESSING}.
     *
     * @param accountId идентификатор счёта
     * @param cardId идентификатор карты
     * @param type тип транзакции
     * @param amount сумма транзакции
     * @return созданная и сохранённая транзакция
     */
    private Transaction createTransaction(Long accountId, Long cardId, TransactionType type, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setCardId(cardId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.PROCESSING);
        return transactionRepository.save(transaction);
    }

    /**
     * Обрабатывает подозрительную транзакцию (фрод):
     * блокирует счёт и переводит транзакцию в статус {@link TransactionStatus#FROZEN}.
     *
     * @param transaction подозрительная транзакция
     */
    private void handleFraudulentTransaction(Transaction transaction) {
        log.warn("Обнаружена подозрительная активность по карте {}", transaction.getCardId());
        accountRepository.findById(transaction.getAccountId()).ifPresent(account -> {
            account.setStatus(AccountStatus.BLOCKED);
            accountRepository.save(account);
            transaction.setStatus(TransactionStatus.FROZEN);
            transactionRepository.save(transaction);
            log.warn("Счёт {} заблокирован из-за фрода", account.getId());
        });
    }

    /**
     * Проверяет, является ли текущая транзакция подозрительной по частоте.
     *
     * @param cardId идентификатор карты
     * @return {@code true}, если количество транзакций превысило лимит за установленное окно времени
     */
    private boolean isSuspicious(Long cardId) {
        Instant now = Instant.now();
        Deque<Instant> history = transactionHistory.computeIfAbsent(cardId, k -> new ArrayDeque<>());

        synchronized (history) {
            while (!history.isEmpty() && history.peekFirst().isBefore(now.minusMillis(timeWindowMs))) {
                history.pollFirst();
            }
            history.addLast(now);
            return history.size() > maxTransactions;
        }
    }

    /**
     * Применяет транзакцию к счёту:
     * <ul>
     *     <li>При {@code DEPOSIT} — увеличивает баланс и проверяет кредитные платежи</li>
     *     <li>При {@code WITHDRAW} — уменьшает баланс (при наличии средств)</li>
     * </ul>
     *
     * @param account счёт, к которому применяется транзакция
     * @param transaction текущая транзакция
     */
    private void applyTransaction(Account account, Transaction transaction) {
        switch (transaction.getType()) {
            case DEPOSIT -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                if (Boolean.TRUE.equals(account.getIsRecalc())) {
                    processCreditRepayment(transaction, account);
                }
            }
            case WITHDRAW -> {
                if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
                    log.warn("Недостаточно средств на счёте {} для списания {}", account.getId(), transaction.getAmount());
                    transaction.setStatus(TransactionStatus.CANCELLED);
                    transactionRepository.save(transaction);
                    return;
                }
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
        }
        accountRepository.save(account);
        transaction.setStatus(TransactionStatus.COMPLETE);
        transactionRepository.save(transaction);
    }

    /**
     * Формирует график платежей по кредитному счёту.
     *
     * @param account счёт
     * @param principal сумма кредита
     * @param interestRate процентная ставка (например, 0.12 = 12%)
     * @param monthCount количество месяцев кредитования
     */
    private void createPaymentSchedule(Account account, BigDecimal principal, BigDecimal interestRate, int monthCount) {
        BigDecimal monthlyPrincipal = principal.divide(BigDecimal.valueOf(monthCount), BigDecimal.ROUND_HALF_UP);
        for (int i = 1; i <= monthCount; i++) {
            BigDecimal interest = principal.multiply(interestRate)
                    .divide(BigDecimal.valueOf(MONTHS_IN_YEAR), BigDecimal.ROUND_HALF_UP);
            Payment payment = new Payment();
            payment.setAccountId(account.getId());
            payment.setPaymentDate(LocalDateTime.now().plusMonths(i));
            payment.setAmount(monthlyPrincipal.add(interest));
            payment.setIsCredit(true);
            payment.setType(PaymentType.LOAN_REPAYMENT);
            paymentRepository.save(payment);
            principal = principal.subtract(monthlyPrincipal);
        }
    }

    /**
     * Обрабатывает автоматическое погашение кредита:
     * если на балансе достаточно средств — списывает сумму следующего платежа и помечает его как оплаченный,
     * иначе — помечает платёж как просроченный.
     *
     * @param transaction исходная транзакция
     * @param account счёт, с которого производится погашение
     */
    private void processCreditRepayment(Transaction transaction, Account account) {
        Optional<Payment> nextPaymentOpt =
                paymentRepository.findFirstByAccountIdAndIsCreditTrueAndIsExpiredFalseAndPaymentDateBeforeOrderByPaymentDateAsc(
                        transaction.getAccountId(), LocalDateTime.now());

        if (nextPaymentOpt.isEmpty()) return;

        Payment nextPayment = nextPaymentOpt.get();
        if (account.getBalance().compareTo(nextPayment.getAmount()) >= 0) {
            account.setBalance(account.getBalance().subtract(nextPayment.getAmount()));
            accountRepository.save(account);

            nextPayment.setPayedAt(LocalDateTime.now());
            paymentRepository.save(nextPayment);

            Transaction repaymentTx = new Transaction();
            repaymentTx.setAccountId(account.getId());
            repaymentTx.setType(TransactionType.WITHDRAW);
            repaymentTx.setAmount(nextPayment.getAmount());
            repaymentTx.setStatus(TransactionStatus.COMPLETE);
            transactionRepository.save(repaymentTx);
        } else {
            nextPayment.setIsExpired(true);
            paymentRepository.save(nextPayment);
        }
    }
}

