package ru.t1.account_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Card;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.entity.enums.CardStatus;
import ru.t1.account_processing.entity.enums.PaymentSystem;
import ru.t1.account_processing.exception.AccountIsArrestedException;
import ru.t1.account_processing.exception.AccountNotExistForClientException;
import ru.t1.account_processing.exception.AccountNotFoundException;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.CardRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_ShouldCreateCard_WhenValidParameters() {
        // Arrange
        Long accountId = 1L;
        Long clientId = 100L;
        String paymentSystem = "VISA";

        Account account = new Account();
        account.setId(accountId);
        account.setClientId(clientId);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        // Act
        cardService.createCard(accountId, clientId, paymentSystem);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(cardRepository).save(cardCaptor.capture());

        Card savedCard = cardCaptor.getValue();
        assertNotNull(savedCard);
        assertEquals(accountId, savedCard.getAccountId());
        assertEquals(PaymentSystem.VISA, savedCard.getPaymentSystem());
        assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
        assertNotNull(savedCard.getCardId());
        assertEquals(16, savedCard.getCardId().length());
    }

    @Test
    void createCard_ShouldThrowException_WhenAccountNotFound() {
        // Arrange
        Long accountId = 999L;
        Long clientId = 100L;
        String paymentSystem = "VISA";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> cardService.createCard(accountId, clientId, paymentSystem)
        );

        assertEquals("Счёт не найден: " + accountId, exception.getMessage());
        verify(accountRepository).findById(accountId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowException_WhenAccountArrested() {
        // Arrange
        Long accountId = 1L;
        Long clientId = 100L;
        String paymentSystem = "VISA";

        Account account = new Account();
        account.setId(accountId);
        account.setClientId(clientId);
        account.setStatus(AccountStatus.ARRESTED);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        AccountIsArrestedException exception = assertThrows(
                AccountIsArrestedException.class,
                () -> cardService.createCard(accountId, clientId, paymentSystem)
        );

        assertEquals("Счет " + accountId + " заблокирован", exception.getMessage());
        verify(accountRepository).findById(accountId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowException_WhenAccountNotBelongsToClient() {
        // Arrange
        Long accountId = 1L;
        Long clientId = 100L;
        Long differentClientId = 200L;
        String paymentSystem = "VISA";

        Account account = new Account();
        account.setId(accountId);
        account.setClientId(differentClientId); // Другой клиент
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        AccountNotExistForClientException exception = assertThrows(
                AccountNotExistForClientException.class,
                () -> cardService.createCard(accountId, clientId, paymentSystem)
        );

        assertTrue(exception.getMessage().contains("не принадлежит клиенту"));
        verify(accountRepository).findById(accountId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_ShouldHandleDifferentPaymentSystems() {
        // Arrange
        Long accountId = 1L;
        Long clientId = 100L;

        Account account = new Account();
        account.setId(accountId);
        account.setClientId(clientId);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Test different payment systems
        String[] paymentSystems = {"VISA", "MASTERCARD", "MIR"};

        for (String paymentSystem : paymentSystems) {
            // Act
            cardService.createCard(accountId, clientId, paymentSystem);

            // Assert
            verify(cardRepository, atLeastOnce()).save(argThat(card ->
                    card.getPaymentSystem() == PaymentSystem.valueOf(paymentSystem.toUpperCase())
            ));
        }
    }
}