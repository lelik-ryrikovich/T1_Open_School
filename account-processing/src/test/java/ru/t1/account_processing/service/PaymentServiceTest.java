package ru.t1.account_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Payment;
import ru.t1.account_processing.entity.enums.PaymentType;
import ru.t1.account_processing.exception.AccountNotFoundException;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_ShouldPerformFullRepayment_WhenAmountEqualsDebt() {
        // Arrange
        Long accountId = 1L;
        BigDecimal paymentAmount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("5000.00"));

        Payment unpaidPayment1 = new Payment();
        unpaidPayment1.setId(1L);
        unpaidPayment1.setAccountId(accountId); // Устанавливаем accountId
        unpaidPayment1.setAmount(new BigDecimal("500.00"));
        unpaidPayment1.setPayedAt(null);

        Payment unpaidPayment2 = new Payment();
        unpaidPayment2.setId(2L);
        unpaidPayment2.setAccountId(accountId); // Устанавливаем accountId
        unpaidPayment2.setAmount(new BigDecimal("500.00"));
        unpaidPayment2.setPayedAt(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(paymentRepository.findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(accountId))
                .thenReturn(Arrays.asList(unpaidPayment1, unpaidPayment2));
        when(paymentRepository.findAllByAccountId(accountId))
                .thenReturn(Arrays.asList(unpaidPayment1, unpaidPayment2));

        // Act
        paymentService.processPayment(accountId, paymentAmount);

        // Assert
        verify(accountRepository).findById(accountId);

        // Проверяем обновление баланса
        verify(accountRepository).save(argThat(acc ->
                acc.getBalance().equals(new BigDecimal("4000.00"))
        ));

        // Проверяем создание нового платежа
        verify(paymentRepository).save(argThat(payment ->
                payment.getPayedAt() != null &&
                        payment.getAccountId() != null && // Добавляем проверку на null
                        payment.getAccountId().equals(accountId) &&
                        payment.getAmount().equals(paymentAmount)
        ));

        // Проверяем обновление существующих платежей
        verify(paymentRepository, times(2)).save(argThat(payment ->
                payment.getPayedAt() != null &&
                        payment.getId() != null && // Проверяем что ID не null
                        (payment.getId().equals(1L) || payment.getId().equals(2L))
        ));
    }

    @Test
    void processPayment_ShouldNotPerformRepayment_WhenAmountNotEqualToDebt() {
        // Arrange
        Long accountId = 1L;
        BigDecimal debtAmount = new BigDecimal("1000.00");
        BigDecimal paymentAmount = new BigDecimal("500.00"); // Меньше долга

        Account account = new Account();
        account.setId(accountId);

        Payment unpaidPayment = new Payment();
        unpaidPayment.setId(1L);
        unpaidPayment.setAmount(debtAmount);
        unpaidPayment.setPayedAt(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(paymentRepository.findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(accountId))
                .thenReturn(List.of(unpaidPayment));

        // Act
        paymentService.processPayment(accountId, paymentAmount);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(paymentRepository).findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(accountId);

        // Verify no balance update
        verify(accountRepository, never()).save(any(Account.class));
        // Verify no payment updates
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldNotPerformRepayment_WhenNoDebt() {
        // Arrange
        Long accountId = 1L;
        BigDecimal paymentAmount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(paymentRepository.findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(accountId))
                .thenReturn(List.of()); // Нет задолженности

        // Act
        paymentService.processPayment(accountId, paymentAmount);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(paymentRepository).findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(accountId);

        // Verify no updates
        verify(accountRepository, never()).save(any(Account.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenAccountNotFound() {
        // Arrange
        Long accountId = 999L;
        BigDecimal paymentAmount = new BigDecimal("1000.00");

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> paymentService.processPayment(accountId, paymentAmount)
        );

        assertEquals("Счёт не найден: " + accountId, exception.getMessage());
        verify(accountRepository).findById(accountId);
        verify(paymentRepository, never()).findAllByAccountIdAndIsCreditTrueAndPayedAtIsNull(anyLong());
    }
}