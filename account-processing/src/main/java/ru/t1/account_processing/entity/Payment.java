package ru.t1.account_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.account_processing.entity.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность платежа.
 * Содержит данные о движении средств по счету.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    /** Уникальный идентификатор платежа */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Идентификатор счета, по которому совершен платеж */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** Дата создания платежа */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    /** Сумма платежа */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Флаг: является ли платеж кредитным */
    @Column(name = "is_credit", nullable = false)
    private Boolean isCredit;

    /** Дата фактической оплаты */
    @Column(name = "payed_at")
    private LocalDateTime payedAt;

    /** Тип платежа (например, перевод, пополнение, снятие) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;
}
