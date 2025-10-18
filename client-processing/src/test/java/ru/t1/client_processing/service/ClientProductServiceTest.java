package ru.t1.client_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.client_processing.dto.ClientProductRequest;
import ru.t1.client_processing.dto.ClientProductResponse;
import ru.t1.client_processing.entity.Client;
import ru.t1.client_processing.entity.ClientProduct;
import ru.t1.client_processing.entity.Product;
import ru.t1.client_processing.entity.enums.ProductKey;
import ru.t1.client_processing.entity.enums.ProductStatus;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.exception.ClientProductAlreadyExistsException;
import ru.t1.client_processing.exception.ClientProductNotFoundException;
import ru.t1.client_processing.exception.ProductNotFoundException;
import ru.t1.client_processing.kafka.KafkaProducerService;
import ru.t1.client_processing.repository.ClientProductRepository;
import ru.t1.client_processing.repository.ClientRepository;
import ru.t1.client_processing.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientProductServiceTest {

    @Mock
    private ClientProductRepository clientProductRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ClientProductService clientProductService;

    private final String CLIENT_PRODUCTS_TOPIC = "client-products-topic";
    private final String CLIENT_CREDIT_PRODUCTS_TOPIC = "client-credit-products-topic";

    private void setupTopics() {
        ReflectionTestUtils.setField(clientProductService, "clientProductsTopic", CLIENT_PRODUCTS_TOPIC);
        ReflectionTestUtils.setField(clientProductService, "clientCreditProductsTopic", CLIENT_CREDIT_PRODUCTS_TOPIC);
    }

    @Test
    void addProductToClient_ShouldAddProduct_WhenValidRequest() {
        // Arrange
        setupTopics();

        Long clientId = 1L;
        Long productId = 1L;

        ClientProductRequest request = new ClientProductRequest();
        request.setClientId(clientId);
        request.setProductId(productId);
        request.setStatus("ACTIVE");

        Client client = new Client();
        client.setId(clientId);
        client.setClientId("770100000001");

        Product product = new Product();
        product.setId(productId);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        ClientProduct savedClientProduct = new ClientProduct();
        savedClientProduct.setId(1L);
        savedClientProduct.setClient(client);
        savedClientProduct.setProduct(product);
        savedClientProduct.setOpenDate(LocalDateTime.now());
        savedClientProduct.setStatus(ProductStatus.ACTIVE);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(clientProductRepository.existsByClientIdAndProductId(clientId, productId)).thenReturn(false);
        when(clientProductRepository.save(any(ClientProduct.class))).thenReturn(savedClientProduct);

        // Act
        ClientProductResponse result = clientProductService.addProductToClient(request);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(productId, result.getProductId());
        assertEquals("Debit Card", result.getProductName());
        assertEquals(ProductKey.DC, result.getProductKey());

        verify(clientRepository).findById(clientId);
        verify(productRepository).findById(productId);
        verify(clientProductRepository).existsByClientIdAndProductId(clientId, productId);
        verify(clientProductRepository).save(any(ClientProduct.class));
        verify(kafkaProducerService).sendMessage(eq(CLIENT_PRODUCTS_TOPIC), any());
    }

    @Test
    void addProductToClient_ShouldThrowException_WhenClientNotFound() {
        // Arrange
        Long clientId = 999L;
        ClientProductRequest request = new ClientProductRequest();
        request.setClientId(clientId);
        request.setProductId(1L);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientProductService.addProductToClient(request)
        );

        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepository).findById(clientId);
        verify(productRepository, never()).findById(anyLong());
        verify(clientProductRepository, never()).save(any(ClientProduct.class));
    }

    @Test
    void addProductToClient_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        Long clientId = 1L;
        Long productId = 999L;

        ClientProductRequest request = new ClientProductRequest();
        request.setClientId(clientId);
        request.setProductId(productId);

        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> clientProductService.addProductToClient(request)
        );

        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(clientRepository).findById(clientId);
        verify(productRepository).findById(productId);
        verify(clientProductRepository, never()).save(any(ClientProduct.class));
    }

    @Test
    void addProductToClient_ShouldThrowException_WhenProductAlreadyExistsForClient() {
        // Arrange
        Long clientId = 1L;
        Long productId = 1L;

        ClientProductRequest request = new ClientProductRequest();
        request.setClientId(clientId);
        request.setProductId(productId);

        Client client = new Client();
        client.setId(clientId);
        client.setClientId("770100000001");

        Product product = new Product();
        product.setId(productId);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(clientProductRepository.existsByClientIdAndProductId(clientId, productId)).thenReturn(true);

        // Act & Assert
        ClientProductAlreadyExistsException exception = assertThrows(
                ClientProductAlreadyExistsException.class,
                () -> clientProductService.addProductToClient(request)
        );

        assertTrue(exception.getMessage().contains("already exists for client"));
        verify(clientRepository).findById(clientId);
        verify(productRepository).findById(productId);
        verify(clientProductRepository).existsByClientIdAndProductId(clientId, productId);
        verify(clientProductRepository, never()).save(any(ClientProduct.class));
    }

    @Test
    void getClientProducts_ShouldReturnProducts_WhenClientExists() {
        // Arrange
        Long clientId = 1L;

        Client client = new Client();
        client.setId(clientId);

        Product product = new Product();
        product.setId(1L);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setId(1L);
        clientProduct.setClient(client);
        clientProduct.setProduct(product);
        clientProduct.setStatus(ProductStatus.ACTIVE);

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(clientProductRepository.findByClientId(clientId)).thenReturn(List.of(clientProduct));

        // Act
        List<ClientProductResponse> result = clientProductService.getClientProducts(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Debit Card", result.get(0).getProductName());
        assertEquals(ProductKey.DC, result.get(0).getProductKey());

        verify(clientRepository).existsById(clientId);
        verify(clientProductRepository).findByClientId(clientId);
    }

    @Test
    void getClientProducts_ShouldThrowException_WhenClientNotFound() {
        // Arrange
        Long clientId = 999L;

        when(clientRepository.existsById(clientId)).thenReturn(false);

        // Act & Assert
        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientProductService.getClientProducts(clientId)
        );

        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepository).existsById(clientId);
        verify(clientProductRepository, never()).findByClientId(anyLong());
    }

    @Test
    void getClientProduct_ShouldReturnClientProduct_WhenExists() {
        // Arrange
        Long clientProductId = 1L;

        Client client = new Client();
        client.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Credit Card");
        product.setKey(ProductKey.CC);

        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setId(clientProductId);
        clientProduct.setClient(client);
        clientProduct.setProduct(product);
        clientProduct.setStatus(ProductStatus.ACTIVE);

        when(clientProductRepository.findById(clientProductId)).thenReturn(Optional.of(clientProduct));

        // Act
        ClientProductResponse result = clientProductService.getClientProduct(clientProductId);

        // Assert
        assertNotNull(result);
        assertEquals(clientProductId, result.getId());
        assertEquals("Credit Card", result.getProductName());
        assertEquals(ProductKey.CC, result.getProductKey());

        verify(clientProductRepository).findById(clientProductId);
    }

    @Test
    void getClientProduct_ShouldThrowException_WhenNotFound() {
        // Arrange
        Long clientProductId = 999L;

        when(clientProductRepository.findById(clientProductId)).thenReturn(Optional.empty());

        // Act & Assert
        ClientProductNotFoundException exception = assertThrows(
                ClientProductNotFoundException.class,
                () -> clientProductService.getClientProduct(clientProductId)
        );

        assertEquals("Client product not found with id: " + clientProductId, exception.getMessage());
        verify(clientProductRepository).findById(clientProductId);
    }

    @Test
    void updateClientProduct_ShouldUpdate_WhenClientProductExists() {
        // Arrange
        setupTopics();

        Long clientProductId = 1L;

        ClientProductRequest request = new ClientProductRequest();
        request.setStatus("CLOSED");
        request.setCloseDate(LocalDateTime.now());

        Client client = new Client();
        client.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        ClientProduct existingClientProduct = new ClientProduct();
        existingClientProduct.setId(clientProductId);
        existingClientProduct.setClient(client);
        existingClientProduct.setProduct(product);
        existingClientProduct.setStatus(ProductStatus.ACTIVE);

        when(clientProductRepository.findById(clientProductId)).thenReturn(Optional.of(existingClientProduct));
        when(clientProductRepository.save(existingClientProduct)).thenReturn(existingClientProduct);

        // Act
        ClientProductResponse result = clientProductService.updateClientProduct(clientProductId, request);

        // Assert
        assertNotNull(result);
        assertEquals(ProductStatus.CLOSED, existingClientProduct.getStatus());
        assertNotNull(existingClientProduct.getCloseDate());

        verify(clientProductRepository).findById(clientProductId);
        verify(clientProductRepository).save(existingClientProduct);
        verify(kafkaProducerService).sendMessage(eq(CLIENT_PRODUCTS_TOPIC), any());
    }

    @Test
    void updateClientProduct_ShouldNotUpdate_WhenFieldsAreNull() {
        // Arrange
        Long clientProductId = 1L;

        ClientProductRequest request = new ClientProductRequest(); // все поля null

        Client client = new Client();
        client.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        ClientProduct existingClientProduct = new ClientProduct();
        existingClientProduct.setId(clientProductId);
        existingClientProduct.setClient(client);
        existingClientProduct.setProduct(product);
        existingClientProduct.setStatus(ProductStatus.ACTIVE);
        existingClientProduct.setCloseDate(null);

        when(clientProductRepository.findById(clientProductId)).thenReturn(Optional.of(existingClientProduct));
        when(clientProductRepository.save(existingClientProduct)).thenReturn(existingClientProduct);

        // Act
        ClientProductResponse result = clientProductService.updateClientProduct(clientProductId, request);

        // Assert
        assertNotNull(result);
        assertEquals(ProductStatus.ACTIVE, existingClientProduct.getStatus()); // статус не изменился
        assertNull(existingClientProduct.getCloseDate()); // closeDate не изменился

        verify(clientProductRepository).findById(clientProductId);
        verify(clientProductRepository).save(existingClientProduct);
    }

    @Test
    void removeProductFromClient_ShouldDelete_WhenClientProductExists() {
        // Arrange
        setupTopics();

        Long clientProductId = 1L;

        Client client = new Client();
        client.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);

        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setId(clientProductId);
        clientProduct.setClient(client);
        clientProduct.setProduct(product);
        clientProduct.setStatus(ProductStatus.ACTIVE);

        when(clientProductRepository.findById(clientProductId)).thenReturn(Optional.of(clientProduct));

        // Act
        clientProductService.removeProductFromClient(clientProductId);

        // Assert
        verify(clientProductRepository).findById(clientProductId);
        verify(clientProductRepository).delete(clientProduct);
        verify(kafkaProducerService).sendMessage(eq(CLIENT_PRODUCTS_TOPIC), any());
    }

    @Test
    void removeProductFromClient_ShouldThrowException_WhenNotFound() {
        // Arrange
        Long clientProductId = 999L;

        when(clientProductRepository.findById(clientProductId)).thenReturn(Optional.empty());

        // Act & Assert
        ClientProductNotFoundException exception = assertThrows(
                ClientProductNotFoundException.class,
                () -> clientProductService.removeProductFromClient(clientProductId)
        );

        assertEquals("Client product not found with id: " + clientProductId, exception.getMessage());
        verify(clientProductRepository).findById(clientProductId);
        verify(clientProductRepository, never()).delete(any(ClientProduct.class));
    }
}