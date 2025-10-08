package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.account_processing.service.AccountService;
import ru.t1.dto.KafkaMessageClientProduct;

import java.math.BigDecimal;

/**
 * Kafka-консьюмер для обработки сообщений из топика {@code client_products}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerAccountService {
    /** Сервис для работы с банковскими счетами. */
    private final AccountService accountService;

    /**
     * Обработка входящих сообщений из Kafka-топика {@code client_products}.
     *
     * @param message сообщение о продукте клиента из Kafka
     */
    @KafkaListener(topics = "client_products", groupId = "account-processing-group")
    public void consume(KafkaMessageClientProduct message) {
        log.info("Получено сообщение из Kafka: {}", message);
        if ("CREATE".equalsIgnoreCase(message.getOperation())) {
            accountService.createAccountForClientProduct(message.getClientId(), message.getProductId());
        } else {
            log.info("Сообщение с операцией {} пока не обрабатывается", message.getOperation());
        }
    }
}
