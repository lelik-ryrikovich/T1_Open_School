package ru.t1.account_processing.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.t1.client.ProcessingHttpClient;
import ru.t1.dto.ProductRegistryInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CreditProcessingClient.class})
@ActiveProfiles("test")
class CreditProcessingClientIntegrationTest {

    @Autowired
    private CreditProcessingClient creditProcessingClient;

    @MockBean
    private ProcessingHttpClient processingHttpClient;

    @Test
    void getProductRegistryByAccount_ShouldReturnProductRegistryInfo_WhenValidAccountId() {
        // Arrange
        Long accountId = 12345L;
        String expectedUrl = "http://localhost:8083/api/product-registry/get/by-accountId/{accountId}";

        ProductRegistryInfo expectedResponse = new ProductRegistryInfo();
        expectedResponse.setId(1L);
        expectedResponse.setAccountId(accountId);
        expectedResponse.setClientId(100L);
        expectedResponse.setProductId(200L);
        expectedResponse.setAmount(new BigDecimal("50000.00"));
        expectedResponse.setInterestRate(new BigDecimal("15.00"));
        expectedResponse.setMonthCount(24);
        expectedResponse.setOpenDate(LocalDate.of(2024, 1, 1));

        when(processingHttpClient.sendGetProductRegistryByAccountRequest(eq(expectedUrl), anyMap()))
                .thenReturn(expectedResponse);

        // Act
        ProductRegistryInfo result = creditProcessingClient.getProductRegistryByAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(expectedResponse.getAmount(), result.getAmount());
        assertEquals(expectedResponse.getInterestRate(), result.getInterestRate());

        verify(processingHttpClient).sendGetProductRegistryByAccountRequest(eq(expectedUrl), anyMap());
    }

    @Test
    void getProductRegistryByAccount_ShouldHandleNullResponse() {
        // Arrange
        Long accountId = 99999L;
        String expectedUrl = "http://localhost:8083/api/product-registry/get/by-accountId/{accountId}";

        when(processingHttpClient.sendGetProductRegistryByAccountRequest(eq(expectedUrl), anyMap()))
                .thenReturn(null);

        // Act
        ProductRegistryInfo result = creditProcessingClient.getProductRegistryByAccount(accountId);

        // Assert
        assertNull(result);
        verify(processingHttpClient).sendGetProductRegistryByAccountRequest(eq(expectedUrl), anyMap());
    }

    @Test
    void getProductRegistryByAccount_ShouldUseCorrectUrlAndParameters() {
        // Arrange
        Long accountId = 55555L;
        String expectedUrl = "http://localhost:8083/api/product-registry/get/by-accountId/{accountId}";

        ProductRegistryInfo mockResponse = new ProductRegistryInfo();
        mockResponse.setAccountId(accountId);

        when(processingHttpClient.sendGetProductRegistryByAccountRequest(eq(expectedUrl), anyMap()))
                .thenReturn(mockResponse);

        // Act
        ProductRegistryInfo result = creditProcessingClient.getProductRegistryByAccount(accountId);

        // Assert
        assertNotNull(result);
        // Проверяем что URL формируется правильно и параметры передаются
        verify(processingHttpClient).sendGetProductRegistryByAccountRequest(
                eq(expectedUrl),
                argThat(params ->
                        params.containsKey("accountId") &&
                                params.get("accountId").equals(accountId)
                )
        );
    }
}