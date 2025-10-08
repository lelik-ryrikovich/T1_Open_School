package ru.t1.client_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.aop.LogDatasourceError;
import ru.t1.client_processing.dto.ClientRegistrationRequest;
import ru.t1.client_processing.dto.ClientRegistrationResponse;
import ru.t1.client_processing.entity.Client;
import ru.t1.client_processing.entity.User;
import ru.t1.client_processing.entity.enums.DocumentType;
import ru.t1.client_processing.exception.ClientAlreadyExistsException;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.repository.ClientRepository;
import ru.t1.client_processing.repository.UserRepository;
import ru.t1.client_processing.util.ClientIdGenerator;
import ru.t1.dto.ClientInfoResponse;

import java.util.Optional;

/**
 * Сервис для управления клиентами.
 * Отвечает за регистрацию новых клиентов и получение информации о них.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final BlacklistService blacklistService;
    private final ClientIdGenerator clientIdGenerator;

    /**
     * Регистрация нового клиента.
     *
     * @param request данные для регистрации
     * @return результат регистрации
     * @throws ClientAlreadyExistsException если клиент с таким email или логином уже существует
     */
    @Transactional
    @LogDatasourceError
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest request) {
        log.info("Starting client registration for login: {}", request.getLogin());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ClientAlreadyExistsException("Client with same email are exist");
        }

        if (userRepository.existsByLogin(request.getLogin())) {
            throw new ClientAlreadyExistsException("Client with same login are exist");
        }

        blacklistService.checkBlacklist(DocumentType.valueOf(request.getDocumentType()), request.getDocumentId());

        // Создание User и Client
        User user = createUser(request);
        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());

        Client client = createClient(request, savedUser);
        Client savedClient = clientRepository.save(client);
        log.info("Client registered successfully. ClientId: {}, ClientCode: {}",
                savedClient.getId(), savedClient.getClientId());

        return mapToResponse(savedUser, savedClient);
    }

    private User createUser(ClientRegistrationRequest request) {
        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        return user;
    }

    /**
     * Получение информации о клиенте.
     *
     * @param clientId идентификатор клиента
     * @return DTO с информацией о клиенте
     * @throws ClientNotFoundException если клиент не найден
     */
    @LogDatasourceError
    public ClientInfoResponse getClientInfo(Long clientId) {
        Client client = clientRepository.findById(clientId).
                orElseThrow(() -> new ClientNotFoundException("Клиент с id " + clientId + " не найден"));

        ClientInfoResponse clientInfoResponse = new ClientInfoResponse();
        clientInfoResponse.setFirstName(client.getFirstName());
        clientInfoResponse.setMiddleName(client.getMiddleName());
        clientInfoResponse.setLastName(client.getLastName());
        clientInfoResponse.setDocumentType(String.valueOf(client.getDocumentType()));
        clientInfoResponse.setDocumentId(client.getDocumentId());
        return clientInfoResponse;
    }

    private Client createClient(ClientRegistrationRequest request, User user) {
        Client client = new Client();
        client.setUser(user);
        client.setClientId(clientIdGenerator.generateClientId(request.getDocumentPrefix()));
        client.setFirstName(request.getFirstName());
        client.setMiddleName(request.getMiddleName());
        client.setLastName(request.getLastName());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setDocumentType(DocumentType.valueOf(request.getDocumentType()));
        client.setDocumentId(request.getDocumentId());
        client.setDocumentPrefix(request.getDocumentPrefix());
        client.setDocumentSuffix(request.getDocumentSuffix());
        return client;
    }

    private ClientRegistrationResponse mapToResponse(User user, Client client) {
        ClientRegistrationResponse response = new ClientRegistrationResponse();
        response.setUserId(user.getId());
        response.setLogin(user.getLogin());
        response.setEmail(user.getEmail());
        response.setClientId(client.getClientId());
        return response;
    }
}
