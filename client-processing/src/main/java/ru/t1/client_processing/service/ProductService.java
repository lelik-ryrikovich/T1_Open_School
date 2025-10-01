package ru.t1.client_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.client_processing.dto.ProductRequest;
import ru.t1.client_processing.dto.ProductResponse;
import ru.t1.client_processing.entity.Product;
import ru.t1.client_processing.exception.ProductAlreadyExistsException;
import ru.t1.client_processing.exception.ProductNotFoundException;
import ru.t1.client_processing.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления продуктами.
 * Выполняет CRUD-операции над справочником продуктов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Создание нового продукта.
     *
     * @param request запрос с параметрами продукта
     * @return созданный продукт
     * @throws ProductAlreadyExistsException если продукт с таким именем уже существует
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getName());

        // Проверка на существующий продукт с таким же именем
        if (productRepository.existsByName(request.getName())) {
            throw new ProductAlreadyExistsException("Product with name '" + request.getName() + "' already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setKey(request.getKey());
        product.setCreateDate(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully. ID: {}, ProductId: {}", savedProduct.getId(), savedProduct.getProductId());

        return mapToResponse(savedProduct);
    }

    /**
     * Получение продукта по бизнес-идентификатору.
     *
     * @param productId бизнес-id продукта
     * @return найденный продукт
     * @throws ProductNotFoundException если продукт не найден
     */
    public ProductResponse getProductByProductId(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with productId: " + productId));
        return mapToResponse(product);
    }

    /**
     * Получение всех продуктов.
     *
     * @return список всех продуктов
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Обновление продукта.
     *
     * @param productId бизнес-id продукта
     * @param request новые параметры продукта
     * @return обновлённый продукт
     * @throws ProductNotFoundException если продукт не найден
     */
    @Transactional
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        log.info("Updating product with id: {}", productId);

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with productID: " + productId));

        product.setName(request.getName());
        product.setKey(request.getKey());

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully. ID: {}", productId);
        return mapToResponse(updatedProduct);
    }

    /**
     * Удаление продукта.
     *
     * @param productId бизнес-id продукта
     * @throws ProductNotFoundException если продукт не найден
     */
    @Transactional
    public void deleteProduct(String productId) {
        log.info("Deleting product with id: {}", productId);

        if (!productRepository.existsByProductId(productId)) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        // TODO: Проверить, нет ли связанных ClientProduct перед удалением
        // if (clientProductRepository.existsByProductId(id)) {
        //     throw new ProductInUseException("Cannot delete product - it is in use by clients");
        // }

        productRepository.deleteByProductId(productId);
        log.info("Product deleted successfully. ID: {}", productId);
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

}
