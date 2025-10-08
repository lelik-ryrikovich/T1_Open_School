package ru.t1.credit_processing.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.client.ProcessingHttpClient;
import ru.t1.dto.ClientInfoResponse;

import java.util.Map;

/**
 * Клиент для взаимодействия с микросервисом client-processing (МС-1).
 * Используется для получения информации о клиенте по его идентификатору.
 */
@Component
@RequiredArgsConstructor
public class ClientProcessingClient {
    /** Базовый URL сервиса client-processing (порт 8081). */
    private static final String BASE_URL = "http://localhost:8081/api/clients/get";

    private final ProcessingHttpClient httpClient;

    /**
     * Получение информации о клиенте по ID из МС-1 (Client Processing).
     *
     * @param clientId идентификатор клиента
     * @return данные о клиенте {@link ClientInfoResponse}
     */
    public ClientInfoResponse getClientInfo(Long clientId) {
        String url = BASE_URL + "/{clientId}";
        Map<String, Object> params = Map.of("clientId", clientId);
        return httpClient.sendGetClientInfoRequest(url, params);
    }
}
