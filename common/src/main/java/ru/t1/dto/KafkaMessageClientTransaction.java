package ru.t1.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для передачи транзакций через Kafka.
 */
@Data
public class KafkaMessageClientTransaction {
    /** Уникальный ключ сообщения */
    private UUID messageKey;

    /** Идентификатор счета */
    private Long accountId;

    /** Идентификатор карты */
    private Long cardId;

    /** Тип транзакции (списание или пополнение) */
    private String type;

    /** Сумма транзакции */
    private BigDecimal amount;

    /** Дата транзакции */
    private LocalDateTime timestamp;
}
