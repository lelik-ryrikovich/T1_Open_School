package ru.t1.account_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.account_processing.entity.enums.AccountStatus;

import java.math.BigDecimal;

/**
 * Сущность банковского счета.
 * Хранит информацию о счете клиента, привязанном к продукту.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {
    /** Уникальный идентификатор счета */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Идентификатор клиента */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /** Идентификатор продукта */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Баланс счета */
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    /** Процентная ставка по счету (например, для депозитов) */
    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    /** Флаг пересчета начислений */
    @Column(name = "is_recalc")
    private Boolean isRecalc = false;

    /** Флаг наличия привязанной карты */
    @Column(name = "card_exist")
    private Boolean cardExist = false;

    /** Статус счета */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;
}
