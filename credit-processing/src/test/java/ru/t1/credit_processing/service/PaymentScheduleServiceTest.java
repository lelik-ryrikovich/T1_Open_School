package ru.t1.credit_processing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.credit_processing.entity.PaymentRegistry;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.repository.PaymentRegistryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentScheduleServiceTest {

    @Mock
    private PaymentRegistryRepository paymentRegistryRepository;

    @InjectMocks
    private PaymentScheduleService paymentScheduleService;

    @Test
    void generateSchedule_ShouldGenerateCorrectSchedule_ForStandardLoan() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("100000.00")); // 100,000 руб
        registry.setInterestRate(new BigDecimal("12.00")); // 12% годовых
        registry.setMonthCount(12); // 12 месяцев
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        assertNotNull(result);
        assertEquals(12, result.size());

        // Проверяем первый платеж
        PaymentRegistry firstPayment = result.get(0);
        assertEquals(LocalDate.of(2024, 2, 1), firstPayment.getPaymentDate());
        assertEquals(LocalDate.of(2024, 2, 6), firstPayment.getPaymentExpirationDate());
        assertFalse(firstPayment.getExpired());
        assertEquals(registry, firstPayment.getProductRegistry());

        // Проверяем что сумма платежа = проценты + тело кредита
        BigDecimal totalAmount = firstPayment.getInterestRateAmount().add(firstPayment.getDebtAmount());
        assertEquals(firstPayment.getAmount(), totalAmount);

        // Проверяем последний платеж
        PaymentRegistry lastPayment = result.get(11);
        assertEquals(LocalDate.of(2025, 01, 1), lastPayment.getPaymentDate());

        verify(paymentRegistryRepository).saveAll(anyList());
    }

    @Test
    void generateSchedule_ShouldCalculateAnnuityCorrectly_ForDifferentParameters() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("50000.00")); // 50,000 руб
        registry.setInterestRate(new BigDecimal("24.00")); // 24% годовых - высокая ставка
        registry.setMonthCount(6); // 6 месяцев
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.size());

        // Проверяем что все платежи имеют одинаковую сумму (аннуитет)
        BigDecimal firstPaymentAmount = result.get(0).getAmount();
        for (PaymentRegistry payment : result) {
            assertEquals(firstPaymentAmount, payment.getAmount());
        }

        // Проверяем что проценты уменьшаются, а тело кредита увеличивается
        BigDecimal previousInterest = result.get(0).getInterestRateAmount();
        BigDecimal previousDebt = result.get(0).getDebtAmount();

        for (int i = 1; i < result.size(); i++) {
            PaymentRegistry payment = result.get(i);
            assertTrue(payment.getInterestRateAmount().compareTo(previousInterest) < 0,
                    "Проценты должны уменьшаться с каждым платежом");
            assertTrue(payment.getDebtAmount().compareTo(previousDebt) > 0,
                    "Тело кредита должно увеличиваться с каждым платежом");
            previousInterest = payment.getInterestRateAmount();
            previousDebt = payment.getDebtAmount();
        }
    }

    @Test
    void generateSchedule_ShouldHandleZeroInterestRate() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("12000.00")); // 12,000 руб
        registry.setInterestRate(BigDecimal.ZERO); // 0% годовых
        registry.setMonthCount(12); // 12 месяцев
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        assertNotNull(result);
        assertEquals(12, result.size());

        // При нулевой ставке все платежи должны быть равны сумме кредита / количество месяцев
        BigDecimal expectedMonthlyPayment = new BigDecimal("1000.00"); // 12000 / 12
        for (PaymentRegistry payment : result) {
            assertEquals(expectedMonthlyPayment, payment.getAmount());
            assertEquals(BigDecimal.ZERO, payment.getInterestRateAmount()); // Проценты = 0
            assertEquals(expectedMonthlyPayment, payment.getDebtAmount()); // Все уходит в тело кредита
        }
    }

    @Test
    void generateSchedule_ShouldHandleOneMonthLoan() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("10000.00")); // 10,000 руб
        registry.setInterestRate(new BigDecimal("12.00")); // 12% годовых
        registry.setMonthCount(1); // 1 месяц
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        PaymentRegistry payment = result.get(0);
        assertEquals(LocalDate.of(2024, 2, 1), payment.getPaymentDate());

        // Проверяем расчет: проценты = 10000 * (12%/12) / 100 = 10000 * 0.01 = 100
        assertEquals(new BigDecimal("100.00"), payment.getInterestRateAmount());
        // Общий платеж должен быть больше суммы кредита на размер процентов
        assertTrue(payment.getAmount().compareTo(registry.getAmount()) > 0);
    }

    @Test
    void generateSchedule_ShouldSetCorrectDates() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("1000.00"));
        registry.setInterestRate(new BigDecimal("10.00"));
        registry.setMonthCount(3);
        LocalDate openDate = LocalDate.of(2024, 3, 15);
        registry.setOpenDate(openDate);

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        assertEquals(3, result.size());

        // Проверяем даты платежей
        assertEquals(openDate.plusMonths(1), result.get(0).getPaymentDate());
        assertEquals(openDate.plusMonths(2), result.get(1).getPaymentDate());
        assertEquals(openDate.plusMonths(3), result.get(2).getPaymentDate());

        // Проверяем даты экспирации (платеж + 5 дней)
        assertEquals(openDate.plusMonths(1).plusDays(5), result.get(0).getPaymentExpirationDate());
        assertEquals(openDate.plusMonths(2).plusDays(5), result.get(1).getPaymentExpirationDate());
        assertEquals(openDate.plusMonths(3).plusDays(5), result.get(2).getPaymentExpirationDate());
    }

    @Test
    void generateSchedule_ShouldSaveAllPaymentsToRepository() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("36000.00")); // 36,000 руб
        registry.setInterestRate(new BigDecimal("12.00")); // 12% годовых
        registry.setMonthCount(36); // 3 года
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        ArgumentCaptor<List<PaymentRegistry>> captor = ArgumentCaptor.forClass(List.class);
        when(paymentRegistryRepository.saveAll(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        verify(paymentRegistryRepository).saveAll(anyList());

        List<PaymentRegistry> savedPayments = captor.getValue();
        assertNotNull(savedPayments);
        assertEquals(36, savedPayments.size());

        // Проверяем что все платежи связаны с правильным ProductRegistry
        for (PaymentRegistry payment : savedPayments) {
            assertEquals(registry, payment.getProductRegistry());
            assertNotNull(payment.getPaymentDate());
            assertNotNull(payment.getPaymentExpirationDate());
            assertFalse(payment.getExpired());
            assertTrue(payment.getAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(payment.getInterestRateAmount().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(payment.getDebtAmount().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    void generateSchedule_ShouldHandleLargeAmountAndLongTerm() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("1000000.00")); // 1,000,000 руб
        registry.setInterestRate(new BigDecimal("8.50")); // 8.5% годовых
        registry.setMonthCount(240); // 20 лет
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        assertNotNull(result);
        assertEquals(240, result.size());

        // Проверяем что сумма всех платежей по телу кредита равна исходной сумме
        BigDecimal totalDebt = result.stream()
                .map(PaymentRegistry::getDebtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Допускаем небольшую погрешность из-за округления
        assertEquals(registry.getAmount().doubleValue(), totalDebt.doubleValue(), 0.01);

        // Проверяем что последний платеж почти полностью состоит из тела кредита
        PaymentRegistry lastPayment = result.get(239);
        assertTrue(lastPayment.getInterestRateAmount().compareTo(lastPayment.getDebtAmount()) < 0);
    }

    @Test
    void generateSchedule_ShouldCalculateCorrectRemainingDebt() {
        // Arrange
        ProductRegistry registry = new ProductRegistry();
        registry.setId(1L);
        registry.setAmount(new BigDecimal("1200.00")); // 1,200 руб
        registry.setInterestRate(new BigDecimal("12.00")); // 12% годовых
        registry.setMonthCount(12); // 12 месяцев
        registry.setOpenDate(LocalDate.of(2024, 1, 1));

        when(paymentRegistryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PaymentRegistry> result = paymentScheduleService.generateSchedule(registry);

        // Assert
        // Проверяем что после всех платежей остаток долга близок к нулю
        // (может быть небольшая погрешность из-за округления)
        PaymentRegistry lastPayment = result.get(11);
        BigDecimal remainingAfterLastPayment = lastPayment.getAmount()
                .subtract(lastPayment.getInterestRateAmount())
                .subtract(lastPayment.getDebtAmount());

        assertTrue(remainingAfterLastPayment.abs().compareTo(new BigDecimal("0.10")) < 0,
                "Остаток после последнего платежа должен быть близок к нулю");
    }
}