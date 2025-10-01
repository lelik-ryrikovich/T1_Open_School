package ru.t1.credit_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Реестр платежей
 */
@Entity
@Table(name = "payment_registry")
@Getter
@Setter
public class PaymentRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Реестр продуктов
     */
    @ManyToOne
    @JoinColumn(name = "product_registry_id", nullable = false)
    private ProductRegistry productRegistry;

    /**
     * Дата платежа
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /**
     * Сумма
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Сумма процентной ставки
     */
    @Column(name = "interest_rate_amount")
    private BigDecimal interestRateAmount;

    /**
     * Сумма долга
     */
    @Column(name = "debt_amount")
    private BigDecimal debtAmount;

    /**
     * Статус истекания
     */
    @Column(nullable = false)
    private Boolean expired = false;

    /**
     * Дата истечения срока платежа
     */
    @Column(name = "payment_expiration_date", nullable = false)
    private LocalDate paymentExpirationDate;
}
