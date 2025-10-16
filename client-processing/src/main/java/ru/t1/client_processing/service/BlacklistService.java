package ru.t1.client_processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.starter.aop.annotation.LogDatasourceError;
import ru.t1.client_processing.entity.BlacklistRegistry;
import ru.t1.client_processing.entity.enums.DocumentType;
import ru.t1.client_processing.exception.BlacklistedClientException;
import ru.t1.client_processing.repository.BlacklistRegistryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис для проверки клиентов на наличие в черном списке.
 */
@Service
@Slf4j
public class BlacklistService {
    @Autowired
    BlacklistRegistryRepository blacklistRegistryRepository;

    /**
     * Проверка, заблокирован ли клиент.
     *
     * @param documentType тип документа
     * @param documentId   номер документа
     * @throws BlacklistedClientException если клиент найден в черном списке
     */
    @LogDatasourceError
    public void checkBlacklist(DocumentType documentType, String documentId) {
        Optional<BlacklistRegistry> blacklistEntry = blacklistRegistryRepository
                .findActiveBlacklistEntry(documentType, documentId, LocalDateTime.now());

        if (blacklistEntry.isPresent()) {
            BlacklistRegistry entry = blacklistEntry.get();
            String reason = entry.getReason();
            LocalDateTime expiration = entry.getBlacklistExpirationAt();

            String message = String.format(
                    "Client is blacklisted. Reason: %s. Expiration: %s",
                    reason,
                    expiration == null ? "permanent" : expiration
            );
            throw new BlacklistedClientException(message);
        }
    }

    /**
     * Добавление клиента в черный список
     */
    @Transactional
    public void addToBlacklist(DocumentType documentType, String documentId,
                               String reason, LocalDateTime expirationDate) {
        BlacklistRegistry entry = new BlacklistRegistry();
        entry.setDocumentType(documentType);
        entry.setDocumentId(documentId);
        entry.setBlacklistedAt(LocalDateTime.now());
        entry.setReason(reason);
        entry.setBlacklistExpirationAt(expirationDate);

        blacklistRegistryRepository.save(entry);
        log.info("📝 Added to blacklist: {} {}, reason: {}", documentType, documentId, reason);
    }

    /**
     * Удаление клиента из черного списка
     */
    @Transactional
    public void removeFromBlacklist(DocumentType documentType, String documentId) {
        blacklistRegistryRepository.deleteByDocumentTypeAndDocumentId(documentType, documentId);
        log.info("✅ Removed from blacklist: {} {}", documentType, documentId);
    }
}
