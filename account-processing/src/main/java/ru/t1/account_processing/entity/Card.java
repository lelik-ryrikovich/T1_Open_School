package ru.t1.account_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.account_processing.entity.enums.CardStatus;
import ru.t1.account_processing.entity.enums.PaymentSystem;

/**
 * Сущность банковской карты.
 * Связана с конкретным счетом и содержит реквизиты карты.
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card {
    /** Уникальный идентификатор карты */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Идентификатор счета, к которому привязана карта */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** Уникальный номер карты */
    @Column(name = "card_id", nullable = false, unique = true)
    private String cardId;

    /** Платежная система (VISA, MasterCard, MIR и т.п.) */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_system", nullable = false)
    private PaymentSystem paymentSystem;

    /** Статус карты */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;
}
