/*
package ru.t1.credit_processing.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountProcessingClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8082/api/accounts";

    public Long getAccountId(Long clientId, Long productId) {
        String url = BASE_URL + "/get/by-client/" + clientId + "/product/" + productId;
        return restTemplate.getForObject(url, Long.class);
    }
}
*/
