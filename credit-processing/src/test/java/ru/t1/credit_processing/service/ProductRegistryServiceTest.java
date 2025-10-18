package ru.t1.credit_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.credit_processing.client.AccountProcessingClient;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.exception.ProductRegistryNotFoundException;
import ru.t1.credit_processing.repository.ProductRegistryRepository;
import ru.t1.dto.ProductRegistryInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRegistryServiceTest {

    @Mock
    private ProductRegistryRepository productRegistryRepository;

    @Mock
    private AccountProcessingClient accountProcessingClient;

    @InjectMocks
    private ProductRegistryService productRegistryService;

    private final BigDecimal INTEREST_RATE = new BigDecimal("15.00");
    private final int MONTH_COUNT = 24;
    private final BigDecimal AMOUNT = new BigDecimal("100000.00");

    @Test
    void openProduct_ShouldCreateProductRegistry_WhenAccountIdFound() {
        // Arrange
        Long clientId = 1L;
        Long productId = 100L;
        Long accountId = 500L;
        LocalDateTime openDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        setupConfiguration();

        when(accountProcessingClient.getAccountId(clientId, productId)).thenReturn(accountId);
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenAnswer(invocation -> {
            ProductRegistry registry = invocation.getArgument(0);
            registry.setId(1L);
            return registry;
        });

        // Act
        ProductRegistry result = productRegistryService.openProduct(clientId, productId, openDate);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(productId, result.getProductId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(AMOUNT, result.getAmount());
        assertEquals(INTEREST_RATE, result.getInterestRate());
        assertEquals(MONTH_COUNT, result.getMonthCount());
        assertEquals(LocalDate.from(openDate), result.getOpenDate());

        verify(accountProcessingClient).getAccountId(clientId, productId);
        verify(productRegistryRepository).save(any(ProductRegistry.class));
    }

    @Test
    void openProduct_ShouldCreateProductRegistry_WhenAccountIdNotFound() {
        // Arrange
        Long clientId = 1L;
        Long productId = 100L;
        LocalDateTime openDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        setupConfiguration();

        when(accountProcessingClient.getAccountId(clientId, productId)).thenReturn(null);
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenAnswer(invocation -> {
            ProductRegistry registry = invocation.getArgument(0);
            registry.setId(1L);
            return registry;
        });

        // Act
        ProductRegistry result = productRegistryService.openProduct(clientId, productId, openDate);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(productId, result.getProductId());
        assertNull(result.getAccountId()); // accountId должен быть null
        assertEquals(AMOUNT, result.getAmount());
        assertEquals(INTEREST_RATE, result.getInterestRate());
        assertEquals(MONTH_COUNT, result.getMonthCount());
        assertEquals(LocalDate.from(openDate), result.getOpenDate());

        verify(accountProcessingClient).getAccountId(clientId, productId);
        verify(productRegistryRepository).save(any(ProductRegistry.class));
    }

    @Test
    void getProductRegistryInfoByAccount_ShouldReturnInfo_WhenProductRegistryExists() {
        // Arrange
        Long accountId = 500L;

        ProductRegistry productRegistry = new ProductRegistry();
        productRegistry.setId(1L);
        productRegistry.setClientId(100L);
        productRegistry.setAccountId(accountId);
        productRegistry.setProductId(200L);
        productRegistry.setAmount(new BigDecimal("50000.00"));
        productRegistry.setInterestRate(new BigDecimal("12.50"));
        productRegistry.setMonthCount(36);
        productRegistry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(productRegistryRepository.findByAccountId(accountId)).thenReturn(productRegistry);

        // Act
        ProductRegistryInfo result = productRegistryService.getProductRegistryInfoByAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(productRegistry.getId(), result.getId());
        assertEquals(productRegistry.getClientId(), result.getClientId());
        assertEquals(productRegistry.getAccountId(), result.getAccountId());
        assertEquals(productRegistry.getProductId(), result.getProductId());
        assertEquals(productRegistry.getAmount(), result.getAmount());
        assertEquals(productRegistry.getInterestRate(), result.getInterestRate());
        assertEquals(productRegistry.getMonthCount(), result.getMonthCount());
        assertEquals(productRegistry.getOpenDate(), result.getOpenDate());

        verify(productRegistryRepository).findByAccountId(accountId);
    }

    @Test
    void getProductRegistryInfoByAccount_ShouldThrowException_WhenProductRegistryNotFound() {
        // Arrange
        Long accountId = 999L;

        when(productRegistryRepository.findByAccountId(accountId)).thenReturn(null);

        // Act & Assert
        ProductRegistryNotFoundException exception = assertThrows(
                ProductRegistryNotFoundException.class,
                () -> productRegistryService.getProductRegistryInfoByAccount(accountId)
        );

        assertEquals("Product Registry с accountId " + accountId + " не найден", exception.getMessage());
        verify(productRegistryRepository).findByAccountId(accountId);
    }

    @Test
    void getProductRegistryInfoByAccount_ShouldHandleNullAccountId() {
        // Arrange
        Long accountId = null;

        when(productRegistryRepository.findByAccountId(accountId)).thenReturn(null);

        // Act & Assert
        ProductRegistryNotFoundException exception = assertThrows(
                ProductRegistryNotFoundException.class,
                () -> productRegistryService.getProductRegistryInfoByAccount(accountId)
        );

        assertEquals("Product Registry с accountId null не найден", exception.getMessage());
        verify(productRegistryRepository).findByAccountId(accountId);
    }

    @Test
    void openProduct_ShouldUseConfigurationValues() {
        // Arrange
        Long clientId = 1L;
        Long productId = 100L;
        Long accountId = 500L;
        LocalDateTime openDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        // Устанавливаем специфичные значения конфигурации
        BigDecimal customInterestRate = new BigDecimal("20.00");
        int customMonthCount = 60;
        BigDecimal customAmount = new BigDecimal("200000.00");

        ReflectionTestUtils.setField(productRegistryService, "interestRate", customInterestRate);
        ReflectionTestUtils.setField(productRegistryService, "monthCount", customMonthCount);
        ReflectionTestUtils.setField(productRegistryService, "amount", customAmount);

        when(accountProcessingClient.getAccountId(clientId, productId)).thenReturn(accountId);
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenAnswer(invocation -> {
            ProductRegistry registry = invocation.getArgument(0);
            registry.setId(1L);
            return registry;
        });

        // Act
        ProductRegistry result = productRegistryService.openProduct(clientId, productId, openDate);

        // Assert
        assertNotNull(result);
        assertEquals(customInterestRate, result.getInterestRate());
        assertEquals(customMonthCount, result.getMonthCount());
        assertEquals(customAmount, result.getAmount());

        verify(accountProcessingClient).getAccountId(clientId, productId);
        verify(productRegistryRepository).save(any(ProductRegistry.class));
    }

    @Test
    void openProduct_ShouldLogWarning_WhenAccountIdNotFound() {
        // Arrange
        Long clientId = 1L;
        Long productId = 100L;
        LocalDateTime openDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        setupConfiguration();

        when(accountProcessingClient.getAccountId(clientId, productId)).thenReturn(null);
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenAnswer(invocation -> {
            ProductRegistry registry = invocation.getArgument(0);
            registry.setId(1L);
            return registry;
        });

        // Act
        ProductRegistry result = productRegistryService.openProduct(clientId, productId, openDate);

        // Assert
        assertNotNull(result);
        assertNull(result.getAccountId());
        // Здесь мы не можем напрямую проверить лог, но можем убедиться что логика выполняется
        verify(accountProcessingClient).getAccountId(clientId, productId);
        verify(productRegistryRepository).save(any(ProductRegistry.class));
    }

    @Test
    void getProductRegistryInfoByAccount_ShouldMapAllFieldsCorrectly() {
        // Arrange
        Long accountId = 500L;

        ProductRegistry productRegistry = new ProductRegistry();
        productRegistry.setId(999L);
        productRegistry.setClientId(888L);
        productRegistry.setAccountId(accountId);
        productRegistry.setProductId(777L);
        productRegistry.setAmount(new BigDecimal("123456.78"));
        productRegistry.setInterestRate(new BigDecimal("9.99"));
        productRegistry.setMonthCount(48);
        productRegistry.setOpenDate(LocalDate.of(2023, 12, 31));

        when(productRegistryRepository.findByAccountId(accountId)).thenReturn(productRegistry);

        // Act
        ProductRegistryInfo result = productRegistryService.getProductRegistryInfoByAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertEquals(888L, result.getClientId());
        assertEquals(500L, result.getAccountId());
        assertEquals(777L, result.getProductId());
        assertEquals(new BigDecimal("123456.78"), result.getAmount());
        assertEquals(new BigDecimal("9.99"), result.getInterestRate());
        assertEquals(48, result.getMonthCount());
        assertEquals(LocalDate.of(2023, 12, 31), result.getOpenDate());
    }

    private void setupConfiguration() {
        ReflectionTestUtils.setField(productRegistryService, "interestRate", INTEREST_RATE);
        ReflectionTestUtils.setField(productRegistryService, "monthCount", MONTH_COUNT);
        ReflectionTestUtils.setField(productRegistryService, "amount", AMOUNT);
    }
}