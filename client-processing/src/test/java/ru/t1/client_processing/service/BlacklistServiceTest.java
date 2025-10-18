package ru.t1.client_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.client_processing.entity.BlacklistRegistry;
import ru.t1.client_processing.entity.enums.DocumentType;
import ru.t1.client_processing.exception.BlacklistedClientException;
import ru.t1.client_processing.repository.BlacklistRegistryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {

    @Mock
    private BlacklistRegistryRepository blacklistRegistryRepository;

    @InjectMocks
    private BlacklistService blacklistService;

    @Test
    void shouldThrowExceptionWhenClientInBlacklist() {
        // given
        DocumentType documentType = DocumentType.PASSPORT;
        String documentId = "1234567890";

        BlacklistRegistry blacklistEntry = new BlacklistRegistry();
        blacklistEntry.setDocumentType(documentType);
        blacklistEntry.setDocumentId(documentId);
        blacklistEntry.setReason("Подозрительная активность");
        blacklistEntry.setBlacklistExpirationAt(LocalDateTime.now().plusDays(30));

        when(blacklistRegistryRepository.findActiveBlacklistEntry(
                eq(documentType), eq(documentId), any(LocalDateTime.class))
        ).thenReturn(Optional.of(blacklistEntry));

        // when & then
        BlacklistedClientException exception = assertThrows(BlacklistedClientException.class, () -> {
            blacklistService.checkBlacklist(documentType, documentId);
        });

        assertTrue(exception.getMessage().contains("Client is blacklisted"));
        assertTrue(exception.getMessage().contains("Подозрительная активность"));

        verify(blacklistRegistryRepository).findActiveBlacklistEntry(
                eq(documentType), eq(documentId), any(LocalDateTime.class));
    }

    @Test
    void shouldNotThrowExceptionWhenClientNotInBlacklist() {
        // given
        DocumentType documentType = DocumentType.PASSPORT;
        String documentId = "1234567890";

        when(blacklistRegistryRepository.findActiveBlacklistEntry(
                eq(documentType), eq(documentId), any(LocalDateTime.class))
        ).thenReturn(Optional.empty());

        // when & then - не должно быть исключения
        assertDoesNotThrow(() -> {
            blacklistService.checkBlacklist(documentType, documentId);
        });

        verify(blacklistRegistryRepository).findActiveBlacklistEntry(
                eq(documentType), eq(documentId), any(LocalDateTime.class));
    }

    @Test
    void shouldAddToBlacklist() {
        // given
        DocumentType documentType = DocumentType.PASSPORT;
        String documentId = "1234567890";
        String reason = "Мошенничество";
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);

        when(blacklistRegistryRepository.save(any(BlacklistRegistry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        blacklistService.addToBlacklist(documentType, documentId, reason, expiration);

        // then
        verify(blacklistRegistryRepository).save(any(BlacklistRegistry.class));
    }

    @Test
    void shouldRemoveFromBlacklist() {
        // given
        DocumentType documentType = DocumentType.PASSPORT;
        String documentId = "1234567890";

        // when
        blacklistService.removeFromBlacklist(documentType, documentId);

        // then
        verify(blacklistRegistryRepository).deleteByDocumentTypeAndDocumentId(documentType, documentId);
    }
}