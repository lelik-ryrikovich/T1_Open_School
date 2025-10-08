package ru.t1.client_processing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.aop.HttpIncomeRequestLog;
import ru.t1.client_processing.dto.ClientRegistrationRequest;
import ru.t1.client_processing.dto.ClientRegistrationResponse;
import ru.t1.client_processing.exception.BlacklistedClientException;
import ru.t1.client_processing.exception.ClientAlreadyExistsException;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.service.ClientService;
import ru.t1.dto.ClientInfoResponse;

/**
 * REST-контроллер для работы с клиентами.
 * Реализует регистрацию клиента и получение информации по clientId.
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    /**
     * Регистрация нового клиента.
     *
     * @param request DTO с данными клиента
     * @return DTO с результатом регистрации
     */
    @PostMapping("/register")
    @HttpIncomeRequestLog
    public ResponseEntity<ClientRegistrationResponse> registerClient(
            @Valid @RequestBody ClientRegistrationRequest request) {
        ClientRegistrationResponse response = clientService.registerClient(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение информации о клиенте по clientId.
     *
     * @param clientId идентификатор клиента
     * @return информация о клиенте
     */
    @GetMapping("get/{clientId}")
    @HttpIncomeRequestLog
    public ResponseEntity<ClientInfoResponse> getClientInfo(@PathVariable("clientId") Long clientId) {
        return ResponseEntity.ok(clientService.getClientInfo(clientId));
    }

    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<String> handleClientAreExist(ClientAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(BlacklistedClientException.class)
    public ResponseEntity<String> handleBlacklistedClient(BlacklistedClientException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<String> handleClientNotFound(ClientNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
