package ru.t1.client_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.client_processing.entity.Client;
import ru.t1.client_processing.entity.Role;
import ru.t1.client_processing.entity.User;
import ru.t1.client_processing.entity.enums.DocumentType;
import ru.t1.client_processing.entity.enums.RoleEnum;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.repository.ClientRepository;
import ru.t1.client_processing.repository.RoleRepository;
import ru.t1.client_processing.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientBlockServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private ClientBlockService clientBlockService;

    @Test
    void shouldBlockClientSuccessfully() {
        // given
        Long clientId = 1L;
        String reason = "Подозрительная активность";
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);

        Client client = createClient(clientId);
        Role blockedRole = new Role();
        blockedRole.setName(RoleEnum.ROLE_BLOCKED_CLIENT);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(roleRepository.findByName(RoleEnum.ROLE_BLOCKED_CLIENT))
                .thenReturn(Optional.of(blockedRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        clientBlockService.blockClient(clientId, reason, expiration);

        // then
        verify(clientRepository).findById(clientId);
        verify(roleRepository).findByName(RoleEnum.ROLE_BLOCKED_CLIENT);
        verify(userRepository).save(any(User.class));
        verify(blacklistService).addToBlacklist(
                eq(DocumentType.PASSPORT), eq("1234567890"), eq(reason), eq(expiration));
    }

    @Test
    void shouldThrowExceptionWhenBlockingNonExistentClient() {
        // given
        Long clientId = 999L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ClientNotFoundException.class, () -> {
            clientBlockService.blockClient(clientId, "Reason", LocalDateTime.now());
        });

        verify(clientRepository).findById(clientId);
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUnblockClientSuccessfully() {
        // given
        Long clientId = 1L;
        Client client = createClient(clientId);
        Role currentRole = new Role();
        currentRole.setName(RoleEnum.ROLE_CURRENT_CLIENT);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(roleRepository.findByName(RoleEnum.ROLE_CURRENT_CLIENT))
                .thenReturn(Optional.of(currentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        clientBlockService.unblockClient(clientId);

        // then
        verify(clientRepository).findById(clientId);
        verify(roleRepository).findByName(RoleEnum.ROLE_CURRENT_CLIENT);
        verify(userRepository).save(any(User.class));
        verify(blacklistService).removeFromBlacklist(DocumentType.PASSPORT, "1234567890");
    }

    @Test
    void shouldReturnTrueWhenClientIsBlocked() {
        // given
        Long clientId = 1L;
        Client client = createClient(clientId);

        Role blockedRole = new Role();
        blockedRole.setName(RoleEnum.ROLE_BLOCKED_CLIENT);
        client.getUser().getRoles().add(blockedRole);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // when
        boolean isBlocked = clientBlockService.isClientBlocked(clientId);

        // then
        assertTrue(isBlocked);
        verify(clientRepository).findById(clientId);
    }

    @Test
    void shouldReturnFalseWhenClientIsNotBlocked() {
        // given
        Long clientId = 1L;
        Client client = createClient(clientId); // Только CURRENT_CLIENT роль

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // when
        boolean isBlocked = clientBlockService.isClientBlocked(clientId);

        // then
        assertFalse(isBlocked);
        verify(clientRepository).findById(clientId);
    }

    @Test
    void shouldReturnTrueWhenUserIsBlockedByUsername() {
        // given
        String username = "blockeduser";
        User user = new User();
        user.setLogin(username);

        Role blockedRole = new Role();
        blockedRole.setName(RoleEnum.ROLE_BLOCKED_CLIENT);
        user.setRoles(Set.of(blockedRole));

        when(userRepository.findByLogin(username)).thenReturn(Optional.of(user));

        // when
        boolean isBlocked = clientBlockService.isUserBlockedByUsername(username);

        // then
        assertTrue(isBlocked);
        verify(userRepository).findByLogin(username);
    }

    private Client createClient(Long clientId) {
        Client client = new Client();
        client.setId(clientId);
        client.setDocumentType(DocumentType.PASSPORT);
        client.setDocumentId("1234567890");

        User user = new User();
        user.setId(1L);
        user.setLogin("testuser");

        Role currentRole = new Role();
        currentRole.setName(RoleEnum.ROLE_CURRENT_CLIENT);
        user.setRoles(new HashSet<>(Set.of(currentRole)));

        client.setUser(user);
        return client;
    }
}