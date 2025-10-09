package ru.t1.credit_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.aop.LogDatasourceError;
import ru.t1.credit_processing.client.AccountProcessingClient;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.exception.ProductRegistryNotFoundException;
import ru.t1.credit_processing.repository.ProductRegistryRepository;
import ru.t1.dto.ProductRegistryInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сервис для работы с кредитными продуктами клиента.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRegistryService {
    private final ProductRegistryRepository productRegistryRepository;
    private final AccountProcessingClient accountProcessingClient;

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
     * Открывает новый кредитный продукт {@link ProductRegistry} для клиента.
     *
     * Метод выполняет следующие шаги:
     * <ul>
     *     <li>пытается получить accountId клиента из account-processing;</li>
     *     <li>создаёт новую запись {@link ProductRegistry};</li>
     *     <li>сохраняет её в базе данных;</li>
     *     <li>возвращает сохранённую сущность.</li>
     * </ul>
     *
     * Если accountId не найден, он будет сохранён как {@code null},
     * при этом запись всё равно создаётся.
     *
     * @param clientId идентификатор клиента
     * @param productId идентификатор кредитного продукта
     * @param openDate дата открытия продукта
     * @return сохранённый объект {@link ProductRegistry}
     */
    @Transactional
    public ProductRegistry openProduct(Long clientId, Long productId, LocalDateTime openDate) {

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

        // Все же я решил пытаться доставать accountId, но даже если это не получится, то он будет просто null
        Long accountId = accountProcessingClient.getAccountId(clientId, productId);
        if (accountId == null) {
            log.warn("Не найден accountId для clientId={} и productId={}. Будем считать его за null.",
                    clientId, productId);
        }
        registry.setAccountId(accountId);

        registry.setClientId(clientId);
        registry.setProductId(productId);
        registry.setAmount(amount);
        registry.setOpenDate(LocalDate.from(openDate));
        registry.setInterestRate(interestRate);
        registry.setMonthCount(monthCount);

        ProductRegistry saved = productRegistryRepository.save(registry);
        log.info("Открыт кредитный продукт {} для клиента {}, сумма {}",
                saved.getId(), saved.getClientId(), saved.getAmount());

        return saved;
    }

    /**
     * Получить информацию о продукте по идентификатору счёта.
     *
     * Метод обращается к репозиторию {@link ProductRegistryRepository}, ищет запись
     * {@link ProductRegistry} по accountId и преобразует её в DTO
     * {@link ProductRegistryInfo}. Если запись не найдена, возвращает null
     * и логирует предупреждение.
     *
     * @param accountId идентификатор счёта
     * @return объект {@link ProductRegistryInfo}, содержащий данные о продукте,
     * либо null, если продукт не найден
     */
    @LogDatasourceError
    public ProductRegistryInfo getProductRegistryInfoByAccount (Long accountId) {
        ProductRegistry productRegistry = productRegistryRepository.findByAccountId(accountId);
        if (productRegistry == null) {
            throw new ProductRegistryNotFoundException("Product Registry с accountId " + accountId + " не найден");
        }

        ProductRegistryInfo productRegistryInfo = new ProductRegistryInfo();
        productRegistryInfo.setId(productRegistry.getId());
        productRegistryInfo.setClientId(productRegistry.getClientId());
        productRegistryInfo.setAccountId(productRegistry.getAccountId());
        productRegistryInfo.setProductId(productRegistry.getProductId());
        productRegistryInfo.setInterestRate(productRegistry.getInterestRate());
        productRegistryInfo.setOpenDate(productRegistry.getOpenDate());
        productRegistryInfo.setMonthCount(productRegistry.getMonthCount());
        productRegistryInfo.setAmount(productRegistry.getAmount());
        return productRegistryInfo;
    }
}
