package ru.t1.credit_processing.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Клиент для взаимодействия с микросервисом account-processing (МС-2).
 * Используется для извлечения accountId по clientId и productId.
 */
@Component
public class AccountProcessingClient {
    private final RestTemplate restTemplate = new RestTemplate();

    /** Базовый URL сервиса account-processing */
    private static final String BASE_URL = "http://localhost:8082/api/accounts";

    /**
     * Получение идентификатора счёта по идентификатору клиента и продукта.
     *
     * @param clientId  идентификатор клиента
     * @param productId идентификатор продукта
     * @return идентификатор счёта или {@code null}, если счёт не найден
     */
    public Long getAccountId(Long clientId, Long productId) {
        String url = BASE_URL + "/get/by-client/" + clientId + "/product/" + productId;
        return restTemplate.getForObject(url, Long.class);
    }
}
