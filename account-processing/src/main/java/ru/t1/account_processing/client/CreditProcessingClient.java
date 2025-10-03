package ru.t1.account_processing.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.t1.dto.ProductRegistryInfo;

/**
 * Клиент для взаимодействия с микросервисом Credit Processing.
 * Используется для получения информации о продукте по идентификатору счёта.
 */
@Component
public class CreditProcessingClient {
    private final RestTemplate restTemplate = new RestTemplate();

    /** Базовый URL микросервиса Credit Processing */
    private static final String BASE_URL = "http://localhost:8083/api/product-registry";

    /**
     * Получение информации о продукте по идентификатору счёта.
     *
     * Отправляет GET-запрос в Credit Processing и возвращает объект
     * {@link ProductRegistryInfo}, содержащий сведения о продукте.
     *
     * @param accountId идентификатор счёта
     * @return объект {@link ProductRegistryInfo}, полученный из Credit Processing
     */
    public ProductRegistryInfo getProductRegistryByAccount(Long accountId) {
        String url = BASE_URL + "/get/by-accountId/" + accountId;
        return restTemplate.getForObject(url, ProductRegistryInfo.class);
    }
}
