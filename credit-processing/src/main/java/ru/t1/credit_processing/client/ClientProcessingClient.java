package ru.t1.credit_processing.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.t1.dto.ClientInfoResponse;

/**
 * Клиент для взаимодействия с микросервисом client-processing (МС-1).
 * Используется для получения информации о клиенте по его идентификатору.
 */
@Component
@RequiredArgsConstructor
public class ClientProcessingClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /** Базовый URL сервиса client-processing (порт 8081). */
    private static final String BASE_URL = "http://localhost:8081/api/clients/get";

    /**
     * Получение информации о клиенте по ID из МС-1.
     *
     * @param clientId идентификатор клиента
     * @return данные о клиенте {@link ClientInfoResponse}
     */
    public ClientInfoResponse getClientInfo(Long clientId) {
        String url = BASE_URL + "/" + clientId;
        return restTemplate.getForObject(url, ClientInfoResponse.class);
    }
}
