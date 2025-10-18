package ru.t1.credit_processing.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.t1.client.ProcessingHttpClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AccountProcessingClient.class})
@ActiveProfiles("test")
class AccountProcessingClientIntegrationTest {

    @Autowired
    private AccountProcessingClient accountProcessingClient;

    @MockBean
    private ProcessingHttpClient processingHttpClient;

    @Test
    void getAccountId_ShouldReturnAccountId_WhenValidClientIdAndProductId() {
        // Arrange
        Long clientId = 100L;
        Long productId = 200L;
        Long expectedAccountId = 500L;

        String expectedUrl = "http://localhost:8082/api/accounts/get/by-client/{clientId}/product/{productId}";

        when(processingHttpClient.sendGetAccountIdRequest(eq(expectedUrl), anyMap()))
                .thenReturn(expectedAccountId);

        // Act
        Long result = accountProcessingClient.getAccountId(clientId, productId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAccountId, result);

        verify(processingHttpClient).sendGetAccountIdRequest(
                eq(expectedUrl),
                argThat(params ->
                        params.containsKey("clientId") && params.get("clientId").equals(clientId) &&
                                params.containsKey("productId") && params.get("productId").equals(productId)
                )
        );
    }

    @Test
    void getAccountId_ShouldReturnNull_WhenAccountNotFound() {
        // Arrange
        Long clientId = 999L;
        Long productId = 999L;
        String expectedUrl = "http://localhost:8082/api/accounts/get/by-client/{clientId}/product/{productId}";

        when(processingHttpClient.sendGetAccountIdRequest(eq(expectedUrl), anyMap()))
                .thenReturn(null);

        // Act
        Long result = accountProcessingClient.getAccountId(clientId, productId);

        // Assert
        assertNull(result);
        verify(processingHttpClient).sendGetAccountIdRequest(eq(expectedUrl), anyMap());
    }

    @Test
    void getAccountId_ShouldUseCorrectUrlPattern() {
        // Arrange
        Long clientId = 123L;
        Long productId = 456L;
        String expectedUrl = "http://localhost:8082/api/accounts/get/by-client/{clientId}/product/{productId}";

        when(processingHttpClient.sendGetAccountIdRequest(eq(expectedUrl), anyMap()))
                .thenReturn(789L);

        // Act
        Long result = accountProcessingClient.getAccountId(clientId, productId);

        // Assert
        assertNotNull(result);
        // Проверяем что URL формируется именно в таком формате
        verify(processingHttpClient).sendGetAccountIdRequest(
                eq("http://localhost:8082/api/accounts/get/by-client/{clientId}/product/{productId}"),
                anyMap()
        );
    }
}