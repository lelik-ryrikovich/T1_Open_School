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

/**
 * Сервис для генерации графика платежей по кредитному продукту.
 * Реализует расчёт аннуитетных платежей.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduleService {

    private final PaymentRegistryRepository paymentRegistryRepository;

    /**
     * Генерация графика платежей для указанного продукта.
     *
     * @param registry кредитный продукт {@link ProductRegistry}
     * @return список платежей {@link PaymentRegistry}, сохранённых в БД
     */
    @Transactional
    public List<PaymentRegistry> generateSchedule(ProductRegistry registry) {
        BigDecimal S = registry.getAmount(); // сумма кредита
        BigDecimal annualRate = registry.getInterestRate(); // годовая ставка, напр. 22%
        int n = registry.getMonthCount(); // срок кредита (месяцы)

        // месячная ставка i = годовая / 12 / 100
        BigDecimal i = annualRate.divide(BigDecimal.valueOf(12 * 100), 10, BigDecimal.ROUND_HALF_UP);

        // Формула аннуитетного платежа А = S × [i × (1 + i)^n] / [(1 + i)^n - 1]
        BigDecimal onePlusI = BigDecimal.ONE.add(i); // (1 + i)
        BigDecimal pow = onePlusI.pow(n); // (1 + i)^n
        BigDecimal numerator = S.multiply(i).multiply(pow); // S × [i × (1 + i)^n]
        BigDecimal denominator = pow.subtract(BigDecimal.ONE); // [(1 + i)^n - 1]
        // S × [i × (1 + i)^n] / [(1 + i)^n - 1]
        BigDecimal A = numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP); // фиксированный платёж

        log.info("Расчёт графика: сумма={}, срок={} мес., ставка={}%, платёж={} руб.",
                S, n, annualRate, A);

        List<PaymentRegistry> schedule = new ArrayList<>();
        BigDecimal remainingDebt = S;

        for (int month = 1; month <= n; month++) {
            LocalDate paymentDate = registry.getOpenDate().plusMonths(month);
            LocalDate expirationDate = paymentDate.plusDays(5); // допустим +5 дней до просрочки

            // проценты за месяц = остаток долга × i
            BigDecimal interest = remainingDebt.multiply(i).setScale(2, BigDecimal.ROUND_HALF_UP);

            // тело кредита = A - проценты
            BigDecimal debtPart = A.subtract(interest).setScale(2, BigDecimal.ROUND_HALF_UP);

            remainingDebt = remainingDebt.subtract(debtPart).setScale(2, BigDecimal.ROUND_HALF_UP);

            PaymentRegistry payment = new PaymentRegistry();
            payment.setProductRegistry(registry);
            payment.setPaymentDate(paymentDate);
            payment.setPaymentExpirationDate(expirationDate);
            payment.setAmount(A);
            payment.setInterestRateAmount(interest);
            payment.setDebtAmount(debtPart);
            payment.setExpired(false);

            schedule.add(payment);
        }

        return paymentRegistryRepository.saveAll(schedule);
    }
}
