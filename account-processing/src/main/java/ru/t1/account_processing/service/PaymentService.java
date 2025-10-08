package ru.t1.account_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Payment;
import ru.t1.account_processing.entity.enums.PaymentType;
import ru.t1.account_processing.exception.AccountNotFoundException;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.PaymentRepository;
import ru.t1.aop.LogDatasourceError;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис бизнес-логики для обработки платежей клиентов.
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Проверку существования счёта;</li>
 *   <li>Подсчёт задолженности по кредиту;</li>
 *   <li>Создание и обновление записей {@link Payment};</li>
 *   <li>Перерасчёт баланса счёта после полного погашения.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Обрабатывает платёж по указанному счёту.
     * <p>
     * Если сумма платежа полностью закрывает задолженность, создаёт запись о платеже и
     * обновляет баланс и все непогашенные платежи.
     *
     * @param accountId идентификатор счёта
     * @param amount    сумма платежа
     * @throws AccountNotFoundException если счёт не найден
     */
    @LogDatasourceError
    public void processPayment(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Счёт не найден: " + accountId));

        // Считаем текущую задолженность по кредиту
        BigDecimal debt = paymentRepository.findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(account.getId())
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Текущая задолженность по счёту {} = {}", account.getId(), debt);

        // Если платёж полностью погашает долг
        if (amount.compareTo(debt) == 0 && debt.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Платёж на сумму {} полностью закрывает задолженность по счёту {}.", amount, accountId);
            performFullRepayment(account, amount);
        } else {
            log.warn("Сумма платежа {} не совпадает с задолженностью {} по счёту {}", amount, debt, accountId);
        }
    }

    /**
     * Выполняет полное погашение задолженности по счёту:
     * <ul>
     *     <li>Списывает деньги с баланса счёта;</li>
     *     <li>Создаёт запись о платеже;</li>
     *     <li>Обновляет все непогашенные платежи как оплаченные.</li>
     * </ul>
     *
     * @param account счёт, по которому проводится погашение
     * @param amount  сумма платежа
     */
    private void performFullRepayment(Account account, BigDecimal amount) {
        // Пересчитываем баланс
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Создаём запись о новом платеже
        Payment newPayment = new Payment();
        newPayment.setAccountId(account.getId());
        newPayment.setPaymentDate(LocalDateTime.now());
        newPayment.setAmount(amount);
        newPayment.setIsCredit(true);
        newPayment.setType(PaymentType.LOAN_REPAYMENT);
        newPayment.setPayedAt(LocalDateTime.now());
        paymentRepository.save(newPayment);

        // Закрываем все незакрытые платежи
        List<Payment> existingPayments = paymentRepository.findAllByAccountId(account.getId());
        for (Payment payment : existingPayments) {
            if (payment.getPayedAt() == null) {
                payment.setPayedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        log.info("Все платежи по счёту {} обновлены, задолженность полностью погашена.", account.getId());
    }
}