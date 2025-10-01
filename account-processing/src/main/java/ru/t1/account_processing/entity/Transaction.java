package ru.t1.account_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.account_processing.entity.enums.TransactionStatus;
import ru.t1.account_processing.entity.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность транзакции.
 * Отражает конкретное движение средств, связанное с картой и счетом.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {
    /** Уникальный идентификатор транзакции */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Идентификатор счета */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** Идентификатор карты */
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    /** Тип транзакции (списание, пополнение и т.п.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /** Сумма транзакции */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Статус транзакции */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PROCESSING;

    /** Дата и время транзакции */
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
