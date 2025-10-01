package ru.t1.client_processing.dto;

import lombok.Data;
import ru.t1.client_processing.entity.enums.ProductKey;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private ProductKey key;
    private LocalDateTime createDate;
    private String productId;
}
