package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Payment;
import ru.t1.account_processing.entity.enums.PaymentType;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.PaymentRepository;
import ru.t1.dto.KafkaMessageClientPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kafka-консьюмер для обработки сообщений о платежах из топика {@code client_payments}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientPaymentService {
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Обработка входящего сообщения о платеже.
     *
     * @param message сообщение о платеже
     */
    @KafkaListener(topics = "client_payments", groupId = "account-processing-group")
    public void consume(KafkaMessageClientPayment message) {
        log.info("Получено сообщение из топика client_payments: {}", message);

        // Находим счёт
        Optional<Account> accountOpt = accountRepository.findById(message.getAccountId());
        if (accountOpt.isEmpty()) {
            log.error("Счёт {} не найден", message.getAccountId());
            return;
        }

        Account account = accountOpt.get();

        // Считаем задолженность по кредиту
        BigDecimal debt = paymentRepository.findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(account.getId())
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Текущая задолженность по счёту {} = {}", account.getId(), debt);

        // Проверяем условие: сумма платежа = сумма задолженности
        if (message.getAmount().compareTo(debt) == 0) {
            log.info("Сумма платежа совпала с задолженностью. Обновляем счёт {}.", account.getId());

            // Пересчитываем баланс
            account.setBalance(account.getBalance().subtract(message.getAmount()));
            accountRepository.save(account);

            // Создаём запись о платеже
            Payment newPayment = new Payment();
            newPayment.setAccountId(account.getId());
            newPayment.setPaymentDate(LocalDateTime.now());
            newPayment.setAmount(message.getAmount());
            newPayment.setIsCredit(true);
            newPayment.setType(PaymentType.LOAN_REPAYMENT);
            newPayment.setPayedAt(LocalDateTime.now());
            paymentRepository.save(newPayment);

            // Обновляем все существующие платежи по счёту
            List<Payment> existingPayments = paymentRepository.findAllByAccountId(account.getId());
            for (Payment payment : existingPayments) {
                if (payment.getPayedAt() == null) {
                    payment.setPayedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            }

            log.info("Все платежи по счёту {} обновлены, задолженность закрыта.", account.getId());
        } else {
            log.warn("Сумма платежа {} не совпадает с задолженностью {} по счёту {}",
                    message.getAmount(), debt, account.getId());
        }
    }
}
