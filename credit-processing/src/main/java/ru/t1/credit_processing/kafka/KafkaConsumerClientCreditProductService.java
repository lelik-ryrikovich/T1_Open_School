package ru.t1.credit_processing.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.credit_processing.client.ClientProcessingClient;
import ru.t1.credit_processing.entity.ProductRegistry;
import ru.t1.credit_processing.service.CreditHistoryService;
import ru.t1.credit_processing.service.CreditLimitService;
import ru.t1.credit_processing.service.PaymentScheduleService;
import ru.t1.credit_processing.service.ProductRegistryService;
import ru.t1.dto.ClientInfoResponse;
import ru.t1.dto.KafkaMessageClientProduct;

import java.math.BigDecimal;

/**
 * Kafka-консьюмер для обработки сообщений о кредитных продуктах клиентов.
 *
 * Слушает топик {@code client_credit_products} и обрабатывает события
 * создания новых кредитных продуктов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientCreditProductService {

    private final ClientProcessingClient clientProcessingClient;
    private final CreditLimitService creditLimitService;
    private final CreditHistoryService creditHistoryService;
    private final ProductRegistryService productRegistryService;
    private final PaymentScheduleService paymentScheduleService;

    /** Сумма кредита по умолчанию (берётся из application.yml/properties). */
    @Value("${credit.amount}")
    private BigDecimal amount;

    /**
     * Обработчик сообщений из Kafka.
     * Открывает ProductRegistry и строит по нему платежный график PaymentRegistry.
     *
     * @param message сообщение с данными о клиентском продукте
     */
    @KafkaListener(topics = "client_credit_products", groupId = "credit-processing-group")
    public void consume(KafkaMessageClientProduct message) {
        log.info("Получено сообщение из топика client_credit_products: {}", message);

        if ("CREATE".equalsIgnoreCase(message.getOperation())) {
            // Обращение к МС-1, GET-запрос за ФИО и номером документа
            ClientInfoResponse clientInfo = clientProcessingClient.getClientInfo(message.getClientId());
            if (clientInfo == null) {
                log.warn("Клиент {} не найден в ClientProcessing", message.getClientId());
                return;
            }

            log.info("Данные клиента: {} {} {}, документ {} {}",
                    clientInfo.getLastName(),
                    clientInfo.getFirstName(),
                    clientInfo.getMiddleName(),
                    clientInfo.getDocumentType(),
                    clientInfo.getDocumentId()
            );

            /* Если есть существующие кредитные продукты (A, B …), и новый
            желаемый продукт в сумме (A + B + C) выходит за лимит N,
            устанавливаемый в конфиге, то отказать. */
            if (creditLimitService.isOverLimit(message.getClientId(), amount)) {
                log.warn("Отказ: клиент {} превысил кредитный лимит при запросе суммы {}",
                        message.getClientId(), amount);
                return;
            }

            /* Если есть существующие кредитные продукты И новый продукт в
            суммарной задолженности по продуктам НЕ выходит за лимит И по
            текущим были просрочки, то отказать. */
            if (creditHistoryService.hasExpiredPayments(message.getClientId())) {
                log.warn("Отказ: у клиента {} были просрочки по платежам, новый кредит не одобрен",
                        message.getClientId());
                return;
            }

            log.info("Клиент {} прошёл проверку: сумма {} в пределах лимита и просрочек нет",
                    message.getClientId(), amount);

            ProductRegistry registry = productRegistryService.openProduct(message);

            /*if (registry == null) {
                log.warn("Открытие продукта отменено — accountId не найден");
                return;
            }*/

            log.info("Клиент {} успешно открыл кредитный продукт {} (сумма: {})",
                    message.getClientId(), registry.getProductId(), registry.getAmount());

            paymentScheduleService.generateSchedule(registry);

            log.info("Клиент {} успешно открыл кредитный продукт {} (сумма: {}), график платежей создан",
                    message.getClientId(), registry.getProductId(), registry.getAmount());


        } else {
            log.info("Операция {} пока не поддерживается", message.getOperation());
        }
    }
}
