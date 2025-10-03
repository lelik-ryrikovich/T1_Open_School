package ru.t1.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class KafkaMessageClientPayment {
    private UUID messageKey;
    private Long id;
    private Long accountId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private Boolean isCredit;
    private LocalDateTime payedAt;
    private String type;
    private Boolean isExpired;
}
