package ru.t1.dto;

import lombok.Data;

@Data
public class KafkaMessageClientCard {
    private String operation; // "CREATE"
    private Long clientId;
    private Long accountId;
    private String paymentSystem; // VISA, MASTERCARD, MIR
}