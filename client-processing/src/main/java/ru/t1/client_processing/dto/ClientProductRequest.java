package ru.t1.client_processing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClientProductRequest {
    private Long clientId;
    private Long productId;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private String status;
}
