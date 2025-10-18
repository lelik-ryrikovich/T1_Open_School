package ru.t1.client_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.client_processing.dto.ProductRequest;
import ru.t1.client_processing.entity.Product;
import ru.t1.client_processing.entity.enums.ProductKey;
import ru.t1.client_processing.exception.ProductAlreadyExistsException;
import ru.t1.client_processing.exception.ProductNotFoundException;
import ru.t1.client_processing.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_ShouldCreateProduct_WhenProductDoesNotExist() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Debit Card");
        request.setKey(ProductKey.DC);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Debit Card");
        savedProduct.setKey(ProductKey.DC);
        savedProduct.setCreateDate(LocalDateTime.now());
        savedProduct.setProductId("DC1");

        when(productRepository.existsByName("Debit Card")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        assertEquals("Debit Card", result.getName());
        assertEquals(ProductKey.DC, result.getKey());
        assertNotNull(result.getCreateDate());

        verify(productRepository).existsByName("Debit Card");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowException_WhenProductAlreadyExists() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Debit Card");
        request.setKey(ProductKey.DC);

        when(productRepository.existsByName("Debit Card")).thenReturn(true);

        // Act & Assert
        ProductAlreadyExistsException exception = assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Product with name 'Debit Card' already exists", exception.getMessage());
        verify(productRepository).existsByName("Debit Card");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductByProductId_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        String productId = "DC1";
        Product product = new Product();
        product.setId(1L);
        product.setProductId(productId);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        // Act
        Product result = productService.getProductByProductId(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        verify(productRepository).findByProductId(productId);
    }

    @Test
    void getProductByProductId_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        String productId = "NON_EXISTENT";

        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductByProductId(productId)
        );

        assertEquals("Product not found with productId: " + productId, exception.getMessage());
        verify(productRepository).findByProductId(productId);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Debit Card");
        product1.setKey(ProductKey.DC);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Credit Card");
        product2.setKey(ProductKey.CC);

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_ShouldUpdateProduct_WhenProductExists() {
        // Arrange
        String productId = "DC1";

        ProductRequest request = new ProductRequest();
        request.setName("Updated Debit Card");
        request.setKey(ProductKey.DC);

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setProductId(productId);
        existingProduct.setName("Old Debit Card");
        existingProduct.setKey(ProductKey.DC);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        // Act
        Product result = productService.updateProduct(productId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Debit Card", existingProduct.getName());
        assertEquals(ProductKey.DC, existingProduct.getKey());

        verify(productRepository).findByProductId(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        String productId = "NON_EXISTENT";

        ProductRequest request = new ProductRequest();
        request.setName("Updated Name");
        request.setKey(ProductKey.DC);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(productId, request)
        );

        assertEquals("Product not found with productID: " + productId, exception.getMessage());
        verify(productRepository).findByProductId(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
        // Arrange
        String productId = "DC1";

        when(productRepository.existsByProductId(productId)).thenReturn(true);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).existsByProductId(productId);
        verify(productRepository).deleteByProductId(productId);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        String productId = "NON_EXISTENT";

        when(productRepository.existsByProductId(productId)).thenReturn(false);

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(productId)
        );

        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(productRepository).existsByProductId(productId);
        verify(productRepository, never()).deleteByProductId(anyString());
    }
}