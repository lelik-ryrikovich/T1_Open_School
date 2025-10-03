package ru.t1.credit_processing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.credit_processing.service.ProductRegistryService;
import ru.t1.dto.ProductRegistryInfo;

/**
 * REST-контроллер для работы с реестром продуктов.
 * Предоставляет API для получения информации о продукте по идентификатору счёта.
 */
@RestController
@RequestMapping("api/product-registry")
@RequiredArgsConstructor
public class ProductRegistryController {

    /** Сервис для работы с данными реестра продуктов */
    private final ProductRegistryService productRegistryService;

    /**
     * Получить информацию о продукте по идентификатору счёта.
     *
     * Обращается к сервису {@link ProductRegistryService} и возвращает
     * объект {@link ProductRegistryInfo}, содержащий сведения о продукте,
     * связанном с указанным счётом.
     *
     * @param accountId идентификатор счёта
     * @return HTTP-ответ со статусом 200 OK и телом {@link ProductRegistryInfo}
     */
    @GetMapping("get/by-accountId/{accountId}")
    public ResponseEntity<ProductRegistryInfo> getByAccount(@PathVariable("accountId") Long accountId) {
        return ResponseEntity.ok(productRegistryService.getProductRegistryInfoByAccount(accountId));
    }
}

