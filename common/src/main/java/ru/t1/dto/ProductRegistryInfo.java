package ru.t1.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductRegistryInfo {
    public Long id;
    public Long clientId;
    public Long accountId;
    public Long productId;
    public BigDecimal interestRate;
    public LocalDate openDate;
    public int monthCount;
    public BigDecimal amount;
}
