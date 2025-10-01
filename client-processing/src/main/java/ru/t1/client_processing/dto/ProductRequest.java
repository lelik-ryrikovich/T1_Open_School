package ru.t1.client_processing.dto;

import lombok.Data;
import ru.t1.client_processing.entity.enums.ProductKey;

@Data
public class ProductRequest {
    private String name;
    private ProductKey key;
}
