package ru.t1.client_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.client_processing.dto.ClientRegistrationRequest;
import ru.t1.client_processing.entity.Client;
import ru.t1.client_processing.entity.User;
import ru.t1.client_processing.entity.Role;
import ru.t1.client_processing.entity.enums.RoleEnum;
import ru.t1.client_processing.exception.ClientAlreadyExistsException;
import ru.t1.client_processing.repository.ClientRepository;
import ru.t1.client_processing.repository.UserRepository;
import ru.t1.client_processing.repository.RoleRepository;
import ru.t1.client_processing.util.ClientIdGenerator;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private ClientIdGenerator clientIdGenerator;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void shouldRegisterClientSuccessfully() {
        // given
        ClientRegistrationRequest request = new ClientRegistrationRequest();
        request.setLogin("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setMiddleName("Middle");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setDocumentType("PASSPORT");
        request.setDocumentId("1234567890");
        request.setDocumentPrefix("77");
        request.setDocumentSuffix("01");

        Role clientRole = new Role();
        clientRole.setName(RoleEnum.ROLE_CURRENT_CLIENT);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByLogin(request.getLogin())).thenReturn(false);
        when(roleRepository.findByName(RoleEnum.ROLE_CURRENT_CLIENT))
                .thenReturn(Optional.of(clientRole));
        when(clientIdGenerator.generateClientId(request.getDocumentPrefix()))
                .thenReturn("770100000001");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(1L);
            return client;
        });

        // when
        var response = clientService.registerClient(request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getLogin());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("770100000001", response.getClientId());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByLogin(request.getLogin());
        verify(blacklistService).checkBlacklist(any(), any());
        verify(userRepository).save(any(User.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // given
        ClientRegistrationRequest request = new ClientRegistrationRequest();
        request.setEmail("existing@example.com");
        request.setLogin("newuser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // when & then
        assertThrows(ClientAlreadyExistsException.class, () -> {
            clientService.registerClient(request);
        });

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).existsByLogin(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenLoginAlreadyExists() {
        // given
        ClientRegistrationRequest request = new ClientRegistrationRequest();
        request.setEmail("new@example.com");
        request.setLogin("existinguser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByLogin(request.getLogin())).thenReturn(true);

        // when & then
        assertThrows(ClientAlreadyExistsException.class, () -> {
            clientService.registerClient(request);
        });

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByLogin(request.getLogin());
        verify(userRepository, never()).save(any());
    }
}