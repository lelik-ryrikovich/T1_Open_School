package ru.t1.credit_processing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.credit_processing.repository.PaymentRegistryRepository;

/**
 * Сервис для проверки кредитной истории клиента.
 * Отвечает за поиск просроченных платежей.
 */
@Service
@RequiredArgsConstructor
public class CreditHistoryService {

    private final PaymentRegistryRepository paymentRegistryRepository;

    /**
     * Проверяет, есть ли у клиента просроченные платежи.
     *
     * @param clientId идентификатор клиента
     * @return true, если есть хотя бы один просроченный платёж
     */
    public boolean hasExpiredPayments(Long clientId) {
        return paymentRegistryRepository.existsExpiredPaymentsByClientId(clientId);
    }
}
