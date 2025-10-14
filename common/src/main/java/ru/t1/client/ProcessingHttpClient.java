package ru.t1.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.t1.starter.aop.annotation.HttpOutcomeRequestLog;
import ru.t1.dto.ClientInfoResponse;
import ru.t1.dto.ProductRegistryInfo;

import java.util.Map;

/**
 * Универсальный HTTP-клиент для взаимодействия между микросервисами.
 * <p>
 * Обеспечивает выполнение REST-запросов (в основном GET) к другим сервисам
 * с использованием {@link RestTemplate}.
 * <p>
 * Все методы аннотированы {@link HttpOutcomeRequestLog}, что позволяет
 * автоматически логировать исходящие HTTP-запросы (через AOP-аспект).
 */
@Component
@RequiredArgsConstructor
public class ProcessingHttpClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @HttpOutcomeRequestLog
    public Long sendGetAccountIdRequest(String url, Map<String, Object> params) {
        return restTemplate.getForObject(url, Long.class, params);
    }

    @HttpOutcomeRequestLog
    public ClientInfoResponse sendGetClientInfoRequest(String url, Map<String, Object> params) {
        return restTemplate.getForObject(url, ClientInfoResponse.class, params);
    }

    @HttpOutcomeRequestLog
    public ProductRegistryInfo sendGetProductRegistryByAccountRequest(String url, Map<String, Object> params) {
        return restTemplate.getForObject(url, ProductRegistryInfo.class, params);
    }
}
