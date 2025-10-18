package ru.t1.account_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.repository.AccountRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountForClientProduct_ShouldCreateAccountWithCorrectParameters() {
        // Arrange
        Long clientId = 1L;
        Long productId = 100L;

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // Act
        accountService.createAccountForClientProduct(clientId, productId);

        // Assert
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();

        assertNotNull(savedAccount);
        assertEquals(clientId, savedAccount.getClientId());
        assertEquals(productId, savedAccount.getProductId());
        assertEquals(BigDecimal.ZERO, savedAccount.getBalance());
        assertEquals(BigDecimal.ZERO, savedAccount.getInterestRate());
        assertFalse(savedAccount.getIsRecalc());
        assertFalse(savedAccount.getCardExist());
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
    }

    @Test
    void createAccountForClientProduct_ShouldCallRepositoryOnce() {
        // Arrange
        Long clientId = 1L;
        Long productId = 100L;

        // Act
        accountService.createAccountForClientProduct(clientId, productId);

        // Assert
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}