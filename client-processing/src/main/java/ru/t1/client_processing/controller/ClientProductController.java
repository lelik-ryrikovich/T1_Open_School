package ru.t1.client_processing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.starter.aop.annotation.HttpIncomeRequestLog;
import ru.t1.client_processing.dto.ClientProductRequest;
import ru.t1.client_processing.dto.ClientProductResponse;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.exception.ClientProductAlreadyExistsException;
import ru.t1.client_processing.exception.ClientProductNotFoundException;
import ru.t1.client_processing.exception.ProductNotFoundException;
import ru.t1.client_processing.service.ClientProductService;
import ru.t1.starter.aop.annotation.Metric;

import java.util.List;

/**
 * REST-контроллер для управления продуктами клиента.
 * Поддерживает CRUD-операции для связи клиента и продукта.
 */
@RestController
@RequestMapping("/api/client-products")
@RequiredArgsConstructor
public class ClientProductController {
    private final ClientProductService clientProductService;

    /**
     * Добавление продукта клиенту.
     *
     * @param request DTO с данными продукта клиента
     * @return созданный продукт клиента
     */
    @PostMapping("/create")
    @HttpIncomeRequestLog
    public ResponseEntity<ClientProductResponse> addProductToClient(
            @Valid @RequestBody ClientProductRequest request) {
        ClientProductResponse response = clientProductService.addProductToClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех продуктов клиента по clientId.
     *
     * @param clientId идентификатор клиента
     * @return список продуктов клиента
     */
    @GetMapping("/get-all-client-products-by-client/{clientId}")
    @HttpIncomeRequestLog
    public ResponseEntity<List<ClientProductResponse>> getClientProducts(
            @PathVariable("clientId") Long clientId) {
        List<ClientProductResponse> responses = clientProductService.getClientProducts(clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Получение продукта клиента по идентификатору clientProductId.
     *
     * @param clientProductId идентификатор продукта клиента
     * @return продукт клиента
     */
    @GetMapping("get/{clientProductId}")
    @HttpIncomeRequestLog
    public ResponseEntity<ClientProductResponse> getClientProduct(
            @PathVariable("clientProductId") Long clientProductId) {
        ClientProductResponse response = clientProductService.getClientProduct(clientProductId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление продукта клиента.
     *
     * @param clientProductId идентификатор продукта клиента
     * @param request         новые данные продукта
     * @return обновлённый продукт клиента
     */
    @PutMapping("update/{clientProductId}")
    @HttpIncomeRequestLog
    public ResponseEntity<ClientProductResponse> updateClientProduct(
            @PathVariable("clientProductId") Long clientProductId,
            @Valid @RequestBody ClientProductRequest request) {
        ClientProductResponse response = clientProductService.updateClientProduct(clientProductId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление продукта клиента.
     *
     * @param clientProductId идентификатор продукта клиента
     * @return статус 204 No Content
     */
    @DeleteMapping("delete/{clientProductId}")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> removeProductFromClient(
            @PathVariable("clientProductId") Long clientProductId) {
        clientProductService.removeProductFromClient(clientProductId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ClientProductNotFoundException.class)
    public ResponseEntity<String> handleClientProductNotFound(ClientProductNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({ClientNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<String> handleNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ClientProductAlreadyExistsException.class)
    public ResponseEntity<String> handleClientProductAlreadyExists(ClientProductAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
