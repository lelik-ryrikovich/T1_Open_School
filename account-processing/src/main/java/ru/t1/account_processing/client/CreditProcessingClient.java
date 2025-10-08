package ru.t1.account_processing.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.client.ProcessingHttpClient;
import ru.t1.dto.ProductRegistryInfo;

import java.util.Map;

/**
 * Клиент для взаимодействия с микросервисом Credit Processing (МС-3).
 *
 * Данный класс инкапсулирует логику формирования HTTP-запросов к внешнему сервису Credit Processing
 * и получения данных о зарегистрированных продуктах по идентификатору счёта.
 *
 * Взаимодействие выполняется через {@link ProcessingHttpClient}, обеспечивающий
 * отправку HTTP-запросов и логирование исходящих обращений.
 */
@Component
@RequiredArgsConstructor
public class CreditProcessingClient {
    /** Базовый URL микросервиса Credit Processing */
    private static final String BASE_URL = "http://localhost:8083/api/product-registry";

    /** Клиент для выполнения HTTP-запросов в другие микросервисы. */
    private final ProcessingHttpClient httpClient;

    /**
     * Получение информации о продукте по идентификатору счёта.
     * Отправляет GET-запрос в МС-3 (Credit Processing) и возвращает объект
     * {@link ProductRegistryInfo}, содержащий сведения о продукте.
     *
     * @param accountId идентификатор счёта
     * @return объект {@link ProductRegistryInfo}, полученный из Credit Processing
     */
    public ProductRegistryInfo getProductRegistryByAccount(Long accountId) {
        String url = BASE_URL + "/get/by-accountId/{accountId}";
        Map<String, Object> params = Map.of("accountId", accountId);
        return httpClient.sendGetProductRegistryByAccountRequest(url, params);
    }
}
