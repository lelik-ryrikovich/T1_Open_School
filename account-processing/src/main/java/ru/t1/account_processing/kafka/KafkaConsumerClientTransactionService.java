package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.client.CreditProcessingClient;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Payment;
import ru.t1.account_processing.entity.Transaction;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.entity.enums.PaymentType;
import ru.t1.account_processing.entity.enums.TransactionStatus;
import ru.t1.account_processing.entity.enums.TransactionType;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.PaymentRepository;
import ru.t1.account_processing.repository.TransactionRepository;
import ru.t1.dto.KafkaMessageClientTransaction;
import ru.t1.dto.ProductRegistryInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka-консьюмер для обработки клиентских транзакций из топика {@code client_transactions}.
 *
 * Основные задачи:
 * Сохранение входящих транзакций.
 * Проверка подозрительной активности (частые транзакции за короткое время).
 * Блокировка счетов при выявлении фрода.
 * Обновление баланса счёта и статусов транзакций.
 * Формирование графика кредитных платежей.
 * Обработка погашения кредитов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientTransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final CreditProcessingClient creditProcessingClient;

    /** Константа: количество месяцев в году (12). */
    private static final int MONTHS_IN_YEAR = 12;

    /**
     * Максимальное количество транзакций в окне {@link #timeWindowMs}.
     * При превышении лимита все транзакции замораживаются, счёт блокируется.
     */
    @Value("${fraud.max-transactions}")
    private int maxTransactions;

    /**
     * Временное окно в миллисекундах для анализа частоты транзакций.
     * Если за этот период количество транзакций превысит {@link #maxTransactions},
     * операции замораживаются, а счёт блокируется.
     */
    @Value("${fraud.time-window-ms}")
    private long timeWindowMs;

    /** Карта историй транзакций: cardId → очередь временных меток транзакций. */
    private final ConcurrentHashMap<Long, Deque<Instant>> transactionHistory = new ConcurrentHashMap<>();

    /**
     * Обработка входящего сообщения о транзакции из Kafka.
     *
     * @param message объект с информацией о транзакции {@link KafkaMessageClientTransaction}
     */
    @KafkaListener(topics = "client_transactions", groupId = "account-processing-group")
    public void consume(KafkaMessageClientTransaction message) {
        log.info("Получено сообщение из топика client_transactions: {}", message);

        // сохраняем транзакцию со статусом PROCESSING
        Transaction transaction = new Transaction();
        transaction.setAccountId(message.getAccountId());
        transaction.setCardId(message.getCardId());
        transaction.setType(TransactionType.valueOf(message.getType()));
        transaction.setAmount(message.getAmount());
        transactionRepository.save(transaction);

        // Проверка на подозрительную активность
        if (isSuspicious(message.getCardId())) {
            log.warn("Подозрительная активность по карте {} – транзакции заморожены", message.getCardId());

            // блокируем счёт
            accountRepository.findById(message.getAccountId()).ifPresent(account -> {
                account.setStatus(AccountStatus.BLOCKED);
                accountRepository.save(account);
                log.warn("Счёт {} заблокирован из-за подозрительной активности", account.getId());
                transaction.setStatus(TransactionStatus.FROZEN);
            });
        }

        // находим счёт
        Optional<Account> accountOpt = accountRepository.findById(message.getAccountId());
        if (accountOpt.isEmpty()) {
            log.error("Счёт {} не найден", message.getAccountId());
            transaction.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(transaction);
            return;
        }

        Account account = accountOpt.get();

        // проверяем статус счёта
        if (account.getStatus() == AccountStatus.BLOCKED || account.getStatus() == AccountStatus.ARRESTED) {
            log.warn("Счёт {} заблокирован/арестован, транзакция отклонена", account.getId());
            transaction.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(transaction);
            return;
        }

        // Если счёт кредитный — создаём график платежей
        if (Boolean.TRUE.equals(account.getIsRecalc())) {
            // Пытаемся достать ProductRegistry, чтобы узнать количество месяцев кредита
            ProductRegistryInfo registry = creditProcessingClient.getProductRegistryByAccount(account.getId());
            int monthCount;
            if (registry == null) {
                monthCount = 60; // такое вот дефолтное значение поставим
            } else {
                monthCount = registry.getMonthCount();
            }
            createPaymentSchedule(account, transaction.getAmount(), account.getInterestRate(), monthCount);
        }

        // обновляем баланс в зависимости от типа транзакции
        switch (transaction.getType()) {
            case DEPOSIT -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));

                // если счёт кредитный → проверяем график платежей
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
     * Создание графика платежей для кредитного счёта.
     *
     * @param account счёт
     * @param principal сумма кредита
     * @param interestRate процентная ставка (например, 0.12 = 12%)
     * @param monthCount количество месяцев
     */
    private void createPaymentSchedule(Account account, BigDecimal principal, BigDecimal interestRate, int monthCount) {
        log.info("Создание графика платежей для кредитного счёта {}", account.getId());
        BigDecimal monthlyPrincipal = principal.divide(BigDecimal.valueOf(monthCount), BigDecimal.ROUND_HALF_UP);

        for (int i = 1; i <= monthCount; i++) {
            // начисляем проценты на остаток долга
            BigDecimal interest = principal.multiply(interestRate).divide(BigDecimal.valueOf(MONTHS_IN_YEAR), BigDecimal.ROUND_HALF_UP);

            Payment payment = new Payment();
            payment.setAccountId(account.getId());
            payment.setPaymentDate(LocalDateTime.now().plusMonths(i));
            payment.setAmount(monthlyPrincipal.add(interest));
            payment.setIsCredit(true);
            payment.setType(PaymentType.LOAN_REPAYMENT);

            paymentRepository.save(payment);
            log.info("Создан платёж {} для счёта {} на сумму {}", i, account.getId(), payment.getAmount());

            // уменьшаем остаток
            principal = principal.subtract(monthlyPrincipal);
        }
    }

    /**
     * Проверяет транзакции по карте на подозрительную активность.
     *
     * @param cardId идентификатор карты
     * @return {@code true}, если количество транзакций превысило лимит в окне времени
     */
    private boolean isSuspicious(Long cardId) {
        Instant now = Instant.now();
        Deque<Instant> history = transactionHistory.computeIfAbsent(cardId, k -> new ArrayDeque<>());

        synchronized (history) {
            // убираем старые записи
            while (!history.isEmpty() && history.peekFirst().isBefore(now.minusMillis(timeWindowMs))) {
                history.pollFirst();
            }

            // добавляем новую транзакцию
            history.addLast(now);

            // если превысили лимит
            return history.size() > maxTransactions;
        }
    }

    /**
     * Обрабатывает погашение кредита:
     * Находит ближайший ожидающий платёж.
     * При достаточном балансе — списывает деньги, отмечает платёж как оплаченный.
     * При недостаточном балансе — помечает платёж как просроченный.
     *
     * @param transaction исходная транзакция типа начисление
     * @param account     счёт, к которому привязаны платежи
     */
    private void processCreditRepayment(Transaction transaction, Account account) {
        log.info("Проверка кредитных платежей по счёту {}", transaction.getAccountId());

        // ищем ближайший ожидающий платёж
        Optional<Payment> nextPaymentOpt = paymentRepository.findFirstByAccountIdAndIsCreditTrueAndIsExpiredFalseAndPaymentDateBeforeOrderByPaymentDateAsc(
                transaction.getAccountId(), LocalDateTime.now()
        );

        if (nextPaymentOpt.isEmpty()) {
            log.info("Нет актуальных платежей для счёта {}", transaction.getAccountId());
            return;
        }

        Payment nextPayment = nextPaymentOpt.get();

        // проверяем хватает ли средств
        if (account.getBalance().compareTo(nextPayment.getAmount()) >= 0) {
            // списываем деньги
            account.setBalance(account.getBalance().subtract(nextPayment.getAmount()));
            accountRepository.save(account);

            // отмечаем как оплаченный
            nextPayment.setPayedAt(LocalDateTime.now());
            paymentRepository.save(nextPayment);

            log.info("Списан платёж {} для счёта {} на сумму {}",
                    nextPayment.getId(), account.getId(), nextPayment.getAmount());

            // создаём отдельную транзакцию списания
            Transaction repaymentTx = new Transaction();
            repaymentTx.setAccountId(account.getId());
            repaymentTx.setType(TransactionType.WITHDRAW);
            repaymentTx.setAmount(nextPayment.getAmount());
            repaymentTx.setStatus(TransactionStatus.COMPLETE);
            transactionRepository.save(repaymentTx);

        } else {
            // недостаточно средств → просроченный
            nextPayment.setIsExpired(true);
            paymentRepository.save(nextPayment);

            log.warn("Просрочка по платёжке {} для счёта {} – недостаточно средств", nextPayment.getId(), account.getId());
        }
    }

}
