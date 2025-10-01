package ru.t1.credit_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сущность для хранения сведений о кредитных продуктах клиента.
 */
@Entity
@Table(name = "product_registry")
@Getter
@Setter
public class ProductRegistry {

    /** Уникальный идентификатор записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Идентификатор клиента. */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /** Идентификатор счёта (может быть null). */
    @Column(name = "account_id", nullable = true)
    private Long accountId;

    /** Идентификатор продукта. */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Процентная ставка по кредиту. */
    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    /** Дата открытия продукта. */
    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    /** Количество месяцев (срок кредита). */
    @Column(name = "month_count")
    private int monthCount;

    /** Сумма кредита. */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}
