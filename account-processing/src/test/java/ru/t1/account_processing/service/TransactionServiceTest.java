package ru.t1.account_processing.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.account_processing.client.CreditProcessingClient;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.Payment;
import ru.t1.account_processing.entity.Transaction;
import ru.t1.account_processing.entity.enums.*;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.repository.PaymentRepository;
import ru.t1.account_processing.repository.TransactionRepository;
import ru.t1.dto.ProductRegistryInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CreditProcessingClient creditProcessingClient;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "maxTransactions", 5);
        ReflectionTestUtils.setField(transactionService, "timeWindowMs", 60000L);
    }

    @Test
    void processTransaction_ShouldProcessDeposit_WhenValidParameters() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "DEPOSIT";
        BigDecimal amount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setIsRecalc(false);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        verify(accountRepository).save(argThat(acc ->
                acc.getBalance().equals(new BigDecimal("6000.00"))
        ));

        // Проверяем что транзакция завершена успешно (второй вызов save)
        verify(transactionRepository, atLeastOnce()).save(argThat(transaction ->
                transaction.getStatus() == TransactionStatus.COMPLETE
        ));
    }

    @Test
    void processTransaction_ShouldProcessWithdraw_WhenSufficientBalance() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "WITHDRAW";
        BigDecimal amount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setIsRecalc(false);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        verify(accountRepository).save(argThat(acc ->
                acc.getBalance().equals(new BigDecimal("4000.00"))
        ));

        // Проверяем успешное завершение
        verify(transactionRepository, atLeastOnce()).save(argThat(transaction ->
                transaction.getStatus() == TransactionStatus.COMPLETE
        ));
    }

    @Test
    void processTransaction_ShouldCancelWithdraw_WhenInsufficientBalance() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "WITHDRAW";
        BigDecimal amount = new BigDecimal("6000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        verify(accountRepository, never()).save(any(Account.class));

        // Используем ArgumentCaptor для проверки всех вызовов
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());

        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        // Первая транзакция - PROCESSING, вторая - CANCELLED
        assertEquals(TransactionStatus.CANCELLED, savedTransactions.get(1).getStatus());
    }

    @Test
    void processTransaction_ShouldCancelTransaction_WhenAccountBlocked() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "DEPOSIT";
        BigDecimal amount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        verify(accountRepository, never()).save(any(Account.class));

        // Проверяем что транзакция отменена (второй вызов save)
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());

        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        assertEquals(TransactionStatus.CANCELLED, savedTransactions.get(1).getStatus());
    }

    @Test
    void processTransaction_ShouldHandleFraud_WhenSuspiciousActivity() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "DEPOSIT";
        BigDecimal amount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Симулируем подозрительную активность
        ConcurrentHashMap<Long, Deque<Instant>> transactionHistory =
                (ConcurrentHashMap<Long, Deque<Instant>>) ReflectionTestUtils.getField(transactionService, "transactionHistory");

        Deque<Instant> history = new java.util.ArrayDeque<>();
        for (int i = 0; i < 6; i++) {
            history.add(Instant.now());
        }
        transactionHistory.put(cardId, history);

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        verify(accountRepository).save(argThat(acc ->
                acc.getStatus() == AccountStatus.BLOCKED
        ));

        // Проверяем что транзакция заморожена
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());

        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        assertEquals(TransactionStatus.FROZEN, savedTransactions.get(1).getStatus());
    }

    @Test
    void processTransaction_ShouldCreatePaymentSchedule_ForRecalcAccount() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "DEPOSIT";
        BigDecimal amount = new BigDecimal("10000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setIsRecalc(true);
        account.setInterestRate(new BigDecimal("0.12")); // Убедимся что interestRate не null

        ProductRegistryInfo registryInfo = new ProductRegistryInfo();
        registryInfo.setMonthCount(12);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(creditProcessingClient.getProductRegistryByAccount(accountId)).thenReturn(registryInfo);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        verify(paymentRepository, times(12)).save(any(Payment.class));
        verify(creditProcessingClient).getProductRegistryByAccount(accountId);
    }

    @Test
    void processTransaction_ShouldProcessCreditRepayment_WhenDepositAndRecalc() {
        // Arrange
        Long accountId = 1L;
        Long cardId = 100L;
        String type = "DEPOSIT";
        BigDecimal amount = new BigDecimal("1000.00");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setIsRecalc(true);
        account.setInterestRate(BigDecimal.ZERO); // Добавляем interestRate чтобы избежать NPE

        Payment nextPayment = new Payment();
        nextPayment.setId(1L);
        nextPayment.setAccountId(accountId);
        nextPayment.setAmount(new BigDecimal("500.00"));
        nextPayment.setIsExpired(false);
        nextPayment.setPaymentDate(LocalDateTime.now().minusDays(1));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(paymentRepository.findFirstByAccountIdAndIsCreditTrueAndIsExpiredFalseAndPaymentDateBeforeOrderByPaymentDateAsc(
                any(), any())).thenReturn(Optional.of(nextPayment));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        transactionService.processTransaction(accountId, cardId, type, amount);

        // Assert
        // Проверяем что платеж был обновлен
        verify(paymentRepository).save(argThat(payment ->
                payment.getPayedAt() != null
        ));
    }
}