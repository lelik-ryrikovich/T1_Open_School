package ru.t1.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class KafkaMessageClientProduct {
    private String operation; // "CREATE", "UPDATE", "DELETE"
    private Long clientProductId;
    private Long clientId;
    private Long productId;
    private String productName;
    private String productKey;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private LocalDateTime timestamp;
    private String status;
}

