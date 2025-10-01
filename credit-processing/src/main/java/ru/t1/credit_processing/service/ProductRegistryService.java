package ru.t1.credit_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import ru.t1.credit_processing.client.AccountProcessingClient;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.repository.ProductRegistryRepository;
import ru.t1.dto.KafkaMessageClientProduct;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сервис для работы с кредитными продуктами клиента.
 * Отвечает за открытие нового продукта и сохранение его в БД.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRegistryService {
    private final ProductRegistryRepository productRegistryRepository;
    //private final AccountProcessingClient accountProcessingClient;

    /** Процентная ставка по умолчанию (из настроек). */
    @Value("${credit.interestRate}")
    private BigDecimal interestRate;

    /** Срок кредита в месяцах (из настроек). */
    @Value("${credit.monthCount}")
    private int monthCount;

    /** Сумма кредита (из настроек). */
    @Value("${credit.amount}")
    private BigDecimal amount;

    /**
     * Открытие нового кредитного продукта для клиента.
     *
     * @param message сообщение из Kafka о создании кредитного продукта
     * @return сохранённый {@link ProductRegistry}
     */
    @Transactional
    public ProductRegistry openProduct(KafkaMessageClientProduct message) {

        // Тут важно, чтобы accountId принадлежал clientId
        //Тут можно найти AccountId по совпадению clientId и productId
        /*Long accountId = accountProcessingClient.getAccountId(message.getClientId(), message.getProductId());

        if (accountId == null) {
            log.warn("Не найден accountId для clientId={} и productId={}. Продукт не будет открыт.",
                    message.getClientId(), message.getProductId());
            return null; // или можно вернуть Optional.empty()
        }*/

        ProductRegistry registry = new ProductRegistry();
        /* Я не понял как достать account_id,
        так как МС-2 (account-processing) создает счет (Account) только при сообщениях топика client_products,
        но не при client_credit_products, поэтому я не знаю как мне account_id доставать.
        registry.setAccountId(accountId); */

        registry.setClientId(message.getClientId());
        registry.setProductId(message.getProductId());
        registry.setAmount(amount);
        registry.setOpenDate(LocalDate.from(message.getOpenDate()));
        registry.setInterestRate(interestRate);
        registry.setMonthCount(monthCount);

        ProductRegistry saved = productRegistryRepository.save(registry);
        log.info("Открыт кредитный продукт {} для клиента {}, сумма {}",
                saved.getId(), saved.getClientId(), saved.getAmount());

        return saved;
    }

}
