package ru.t1.client_processing.dto;

import java.time.LocalDateTime;
import lombok.Data;
import ru.t1.client_processing.entity.enums.ProductKey;
import ru.t1.client_processing.entity.enums.ProductStatus;

@Data
public class ClientProductResponse {
    private Long id;
    private Long clientId;
    private Long productId;
    private String productName;
    private ProductKey productKey;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private ProductStatus status;
}
