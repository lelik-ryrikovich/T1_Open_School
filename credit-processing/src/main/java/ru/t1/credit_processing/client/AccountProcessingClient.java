package ru.t1.credit_processing.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.client.ProcessingHttpClient;

import java.util.Map;

/**
 * Клиент для взаимодействия с микросервисом account-processing (МС-2).
 * Используется для извлечения accountId по clientId и productId.
 */
@Component
@RequiredArgsConstructor
public class AccountProcessingClient {
    /** Базовый URL сервиса account-processing */
    private static final String BASE_URL = "http://localhost:8082/api/accounts";

    private final ProcessingHttpClient httpClient;

    /**
     * Получение идентификатора счёта по идентификатору клиента и продукта.
     *
     * @param clientId  идентификатор клиента
     * @param productId идентификатор продукта
     * @return идентификатор счёта или {@code null}, если счёт не найден
     */
    public Long getAccountId(Long clientId, Long productId) {
        String url = BASE_URL + "/get/by-client/{clientId}/product/{productId}";
        Map<String, Object> params = Map.of("clientId", clientId, "productId", productId);
        return httpClient.sendGetAccountIdRequest(url, params);
    }
}
