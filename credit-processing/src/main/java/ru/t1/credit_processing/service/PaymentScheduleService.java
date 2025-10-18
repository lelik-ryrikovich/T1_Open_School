package ru.t1.credit_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.credit_processing.entity.PaymentRegistry;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.repository.PaymentRegistryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduleService {

    private final PaymentRegistryRepository paymentRegistryRepository;

    @Transactional
    public List<PaymentRegistry> generateSchedule(ProductRegistry registry) {
        BigDecimal S = registry.getAmount();
        BigDecimal annualRate = registry.getInterestRate();
        int n = registry.getMonthCount();

        // Обработка нулевой процентной ставки
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return generateZeroInterestSchedule(registry, S, n);
        }

        // месячная ставка i = годовая / 12 / 100
        BigDecimal i = annualRate.divide(BigDecimal.valueOf(12 * 100), 10, BigDecimal.ROUND_HALF_UP);

        // Формула аннуитетного платежа А = S × [i × (1 + i)^n] / [(1 + i)^n - 1]
        BigDecimal onePlusI = BigDecimal.ONE.add(i);
        BigDecimal pow = onePlusI.pow(n);
        BigDecimal numerator = S.multiply(i).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        BigDecimal A = numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP);

        log.info("Расчёт графика: сумма={}, срок={} мес., ставка={}%, платёж={} руб.",
                S, n, annualRate, A);

        return generatePayments(registry, S, n, i, A);
    }

    /**
     * Генерация графика для нулевой процентной ставки
     */
    private List<PaymentRegistry> generateZeroInterestSchedule(ProductRegistry registry, BigDecimal S, int n) {
        log.info("Расчёт графика с нулевой ставкой: сумма={}, срок={} мес.", S, n);

        BigDecimal monthlyPayment = S.divide(BigDecimal.valueOf(n), 2, BigDecimal.ROUND_HALF_UP);
        List<PaymentRegistry> schedule = new ArrayList<>();
        BigDecimal remainingDebt = S;

        for (int month = 1; month <= n; month++) {
            LocalDate paymentDate = registry.getOpenDate().plusMonths(month);
            LocalDate expirationDate = paymentDate.plusDays(5);

            // Для последнего платежа корректируем сумму, чтобы избежать погрешности
            BigDecimal debtPart = (month == n) ? remainingDebt : monthlyPayment;
            BigDecimal interest = BigDecimal.ZERO;

            remainingDebt = remainingDebt.subtract(debtPart);

            PaymentRegistry payment = createPayment(registry, paymentDate, expirationDate,
                    monthlyPayment, interest, debtPart);
            schedule.add(payment);
        }

        return paymentRegistryRepository.saveAll(schedule);
    }

    /**
     * Генерация платежей с процентной ставкой
     */
    private List<PaymentRegistry> generatePayments(ProductRegistry registry, BigDecimal S, int n,
                                                   BigDecimal i, BigDecimal A) {
        List<PaymentRegistry> schedule = new ArrayList<>();
        BigDecimal remainingDebt = S;

        for (int month = 1; month <= n; month++) {
            LocalDate paymentDate = registry.getOpenDate().plusMonths(month);
            LocalDate expirationDate = paymentDate.plusDays(5);

            // проценты за месяц = остаток долга × i
            BigDecimal interest = remainingDebt.multiply(i).setScale(2, BigDecimal.ROUND_HALF_UP);

            // тело кредита = A - проценты
            BigDecimal debtPart = A.subtract(interest).setScale(2, BigDecimal.ROUND_HALF_UP);

            // Для последнего платежа корректируем debtPart, чтобы избежать погрешности
            if (month == n) {
                debtPart = remainingDebt;
            }

            remainingDebt = remainingDebt.subtract(debtPart).setScale(2, BigDecimal.ROUND_HALF_UP);

            PaymentRegistry payment = createPayment(registry, paymentDate, expirationDate, A, interest, debtPart);
            schedule.add(payment);
        }

        return paymentRegistryRepository.saveAll(schedule);
    }

    private PaymentRegistry createPayment(ProductRegistry registry, LocalDate paymentDate,
                                          LocalDate expirationDate, BigDecimal amount,
                                          BigDecimal interest, BigDecimal debtPart) {
        PaymentRegistry payment = new PaymentRegistry();
        payment.setProductRegistry(registry);
        payment.setPaymentDate(paymentDate);
        payment.setPaymentExpirationDate(expirationDate);
        payment.setAmount(amount);
        payment.setInterestRateAmount(interest);
        payment.setDebtAmount(debtPart);
        payment.setExpired(false);
        return payment;
    }
}
