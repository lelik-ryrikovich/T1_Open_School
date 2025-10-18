package ru.t1.credit_processing.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.t1.client.ProcessingHttpClient;
import ru.t1.dto.ClientInfoResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ClientProcessingClient.class})
@ActiveProfiles("test")
class ClientProcessingClientIntegrationTest {

    @Autowired
    private ClientProcessingClient clientProcessingClient;

    @MockBean
    private ProcessingHttpClient processingHttpClient;

    @Test
    void getClientInfo_ShouldReturnClientInfo_WhenValidClientId() {
        // Arrange
        Long clientId = 100L;
        String expectedUrl = "http://localhost:8081/api/clients/get/{clientId}";

        ClientInfoResponse expectedResponse = new ClientInfoResponse();
        expectedResponse.setFirstName("Иван");
        expectedResponse.setLastName("Петров");
        expectedResponse.setMiddleName("Сергеевич");
        expectedResponse.setDocumentType("PASSPORT");
        expectedResponse.setDocumentId("1234567890");

        when(processingHttpClient.sendGetClientInfoRequest(eq(expectedUrl), anyMap()))
                .thenReturn(expectedResponse);

        // Act
        ClientInfoResponse result = clientProcessingClient.getClientInfo(clientId);

        // Assert
        assertNotNull(result);
        assertEquals("Иван", result.getFirstName());
        assertEquals("Петров", result.getLastName());
        assertEquals("Сергеевич", result.getMiddleName());
        assertEquals("PASSPORT", result.getDocumentType());
        assertEquals("1234567890", result.getDocumentId());

        verify(processingHttpClient).sendGetClientInfoRequest(
                eq(expectedUrl),
                argThat(params ->
                        params.containsKey("clientId") && params.get("clientId").equals(clientId)
                )
        );
    }

    @Test
    void getClientInfo_ShouldReturnNull_WhenClientNotFound() {
        // Arrange
        Long clientId = 999L;
        String expectedUrl = "http://localhost:8081/api/clients/get/{clientId}";

        when(processingHttpClient.sendGetClientInfoRequest(eq(expectedUrl), anyMap()))
                .thenReturn(null);

        // Act
        ClientInfoResponse result = clientProcessingClient.getClientInfo(clientId);

        // Assert
        assertNull(result);
        verify(processingHttpClient).sendGetClientInfoRequest(eq(expectedUrl), anyMap());
    }

    @Test
    void getClientInfo_ShouldUseCorrectBaseUrl() {
        // Arrange
        Long clientId = 123L;
        String expectedUrl = "http://localhost:8081/api/clients/get/{clientId}";

        ClientInfoResponse mockResponse = new ClientInfoResponse();
        mockResponse.setFirstName("Test");
        mockResponse.setLastName("User");

        when(processingHttpClient.sendGetClientInfoRequest(eq(expectedUrl), anyMap()))
                .thenReturn(mockResponse);

        // Act
        ClientInfoResponse result = clientProcessingClient.getClientInfo(clientId);

        // Assert
        assertNotNull(result);
        // Проверяем что используется правильный порт (8081 для client-processing)
        verify(processingHttpClient).sendGetClientInfoRequest(
                eq("http://localhost:8081/api/clients/get/{clientId}"),
                anyMap()
        );
    }

    @Test
    void getClientInfo_ShouldHandleMultipleCallsWithDifferentParameters() {
        // Arrange
        Long firstClientId = 1L;
        Long secondClientId = 2L;
        String expectedUrl = "http://localhost:8081/api/clients/get/{clientId}";

        ClientInfoResponse firstResponse = new ClientInfoResponse();
        firstResponse.setFirstName("First");
        firstResponse.setLastName("Client");

        ClientInfoResponse secondResponse = new ClientInfoResponse();
        secondResponse.setFirstName("Second");
        secondResponse.setLastName("Client");

        when(processingHttpClient.sendGetClientInfoRequest(eq(expectedUrl), anyMap()))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        // Act
        ClientInfoResponse firstResult = clientProcessingClient.getClientInfo(firstClientId);
        ClientInfoResponse secondResult = clientProcessingClient.getClientInfo(secondClientId);

        // Assert
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertEquals("First", firstResult.getFirstName());
        assertEquals("Second", secondResult.getFirstName());

        // Проверяем что для каждого clientId был отдельный вызов с правильными параметрами
        verify(processingHttpClient).sendGetClientInfoRequest(
                eq(expectedUrl),
                argThat(params -> params.get("clientId").equals(firstClientId))
        );
        verify(processingHttpClient).sendGetClientInfoRequest(
                eq(expectedUrl),
                argThat(params -> params.get("clientId").equals(secondClientId))
        );
    }
}