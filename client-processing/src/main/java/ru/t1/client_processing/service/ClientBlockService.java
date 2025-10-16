package ru.t1.client_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.client_processing.entity.Client;
import ru.t1.client_processing.entity.Role;
import ru.t1.client_processing.entity.User;
import ru.t1.client_processing.entity.enums.RoleEnum;
import ru.t1.client_processing.exception.ClientNotFoundException;
import ru.t1.client_processing.repository.ClientRepository;
import ru.t1.client_processing.repository.RoleRepository;
import ru.t1.client_processing.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientBlockService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BlacklistService blacklistService;

    /**
     * Блокировка клиента по clientId
     */
    @Transactional
    public void blockClient(Long clientId, String reason, LocalDateTime expirationDate) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Клиент с id " + clientId + " не найден"));

        User user = client.getUser();

        // Находим роль BLOCKED_CLIENT
        Role blockedRole = roleRepository.findByName(RoleEnum.ROLE_BLOCKED_CLIENT)
                .orElseThrow(() -> new RuntimeException("Role ROLE_BLOCKED_CLIENT not found"));

        // Убираем текущую роль и добавляем BLOCKED_CLIENT
        user.getRoles().clear();
        user.getRoles().add(blockedRole);

        userRepository.save(user);

        // Добавляем в черный список
        blacklistService.addToBlacklist(
                client.getDocumentType(),
                client.getDocumentId(),
                reason,
                expirationDate
        );

        log.info("🔒 Client blocked. ClientId: {}, UserId: {}, Reason: {}",
                clientId, user.getId(), reason);
    }

    /**
     * Разблокировка клиента
     */
    @Transactional
    public void unblockClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Клиент с id " + clientId + " не найден"));

        User user = client.getUser();

        // Находим роль CURRENT_CLIENT
        Role currentRole = roleRepository.findByName(RoleEnum.ROLE_CURRENT_CLIENT)
                .orElseThrow(() -> new RuntimeException("Role ROLE_CURRENT_CLIENT not found"));

        // Убираем blocked роль и добавляем текущую
        user.getRoles().clear();
        user.getRoles().add(currentRole);

        userRepository.save(user);

        // Убираем из черного списка
        blacklistService.removeFromBlacklist(client.getDocumentType(), client.getDocumentId());

        log.info("🔓 Client unblocked. ClientId: {}, UserId: {}", clientId, user.getId());
    }

    /**
     * Проверка, заблокирован ли клиент
     */
    public boolean isClientBlocked(Long clientId) {
        return clientRepository.findById(clientId)
                .map(client -> client.getUser().getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleEnum.ROLE_BLOCKED_CLIENT))
                .orElse(false);
    }

    /**
     * Проверка, заблокирован ли пользователь
     */
    public boolean isUserBlocked(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleEnum.ROLE_BLOCKED_CLIENT))
                .orElse(false);
    }

    public boolean isUserBlockedByUsername(String username) {
        return userRepository.findByLogin(username)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleEnum.ROLE_BLOCKED_CLIENT))
                .orElse(false);
    }
}