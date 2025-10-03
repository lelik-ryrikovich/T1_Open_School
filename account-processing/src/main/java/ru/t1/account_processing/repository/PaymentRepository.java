package ru.t1.account_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.account_processing.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByAccountIdAndIsCreditTrueAndIsExpiredFalseAndPaymentDateBeforeOrderByPaymentDateAsc(
            Long accountId, LocalDateTime date
    );

    List<Payment> findAllByAccountId(Long accountId);

    List<Payment> findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(Long accountId);

}
