package ru.t1.credit_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_registry")
@Getter
@Setter
public class PaymentRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_registry_id", nullable = false)
    private ProductRegistry productRegistry;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "interest_rate_amount")
    private BigDecimal interestRateAmount;

    @Column(name = "debt_amount")
    private BigDecimal debtAmount;

    @Column(nullable = false)
    private Boolean expired = false;

    @Column(name = "payment_expiration_date", nullable = false)
    private LocalDate paymentExpirationDate;
}
