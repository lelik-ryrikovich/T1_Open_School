package ru.t1.client_processing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.t1.starter.aop.annotation.HttpIncomeRequestLog;
import ru.t1.client_processing.dto.ProductRequest;
import ru.t1.client_processing.dto.ProductResponse;
import ru.t1.client_processing.entity.Product;
import ru.t1.client_processing.exception.ProductAlreadyExistsException;
import ru.t1.client_processing.exception.ProductNotFoundException;
import ru.t1.client_processing.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер для управления продуктами.
 * Поддерживает CRUD-операции с банковскими продуктами.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * Создание нового продукта.
     *
     * @param request DTO с данными продукта
     * @return созданный продукт
     */
    @PostMapping("/create")
    @HttpIncomeRequestLog
    @PreAuthorize("hasRole('MASTER')") // Только MASTER может создавать
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        ProductResponse response = mapToResponse(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение продукта по productId.
     *
     * @param productId идентификатор продукта
     * @return продукт
     */
    @GetMapping("/get/{productId}")
    @HttpIncomeRequestLog
    public ResponseEntity<ProductResponse> getProductByProductId(@PathVariable("productId") String productId) {
        Product product = productService.getProductByProductId(productId);
        ProductResponse response = mapToResponse(product);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение списка всех продуктов.
     *
     * @return список продуктов
     */
    @GetMapping("/get-all")
    @HttpIncomeRequestLog
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Обновление продукта по productId.
     *
     * @param productId идентификатор продукта
     * @param request   новые данные продукта
     * @return обновлённый продукт
     */
    @PutMapping("/update/{productId}")
    @HttpIncomeRequestLog
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')") // MASTER и GRAND_EMPLOYEE могут редактировать
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("productId") String productId,
            @Valid @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(productId, request);
        ProductResponse response = mapToResponse(product);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление продукта по productId.
     *
     * @param productId идентификатор продукта
     * @return статус 204 No Content
     */
    @DeleteMapping("/delete/{productId}")
    @HttpIncomeRequestLog
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')") // MASTER и GRAND_EMPLOYEE могут удалять
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setKey(product.getKey());
        response.setCreateDate(product.getCreateDate());
        response.setProductId(product.getProductId());
        return response;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFound(ProductNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<String> handleProductAlreadyExists(ProductAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}

