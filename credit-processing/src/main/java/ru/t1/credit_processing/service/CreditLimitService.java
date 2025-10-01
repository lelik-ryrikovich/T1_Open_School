package ru.t1.credit_processing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.repository.ProductRegistryRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сервис для проверки кредитных лимитов клиента.
 * Используется при открытии нового кредита.
 */
@Service
@RequiredArgsConstructor
public class CreditLimitService {

    private final ProductRegistryRepository productRegistryRepository;

    /** Кредитный лимит клиента (берётся из настроек). */
    @Value("${credit.limit}")
    private BigDecimal creditLimit;

    /**
     * Проверяет, не превышает ли сумма существующих и нового кредитов лимит.
     *
     * @param clientId идентификатор клиента
     * @param newProductAmount сумма нового кредитного продукта
     * @return true, если лимит превышен
     */
    public boolean isOverLimit(Long clientId, BigDecimal newProductAmount) {
        List<ProductRegistry> products = productRegistryRepository.findByClientId(clientId);

        BigDecimal totalExisting = products.stream()
                .map(ProductRegistry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithNew = totalExisting.add(newProductAmount);

        return totalWithNew.compareTo(creditLimit) > 0;
    }
}
