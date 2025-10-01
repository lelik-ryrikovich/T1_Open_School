package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.entity.Account;
import ru.t1.account_processing.entity.enums.AccountStatus;
import ru.t1.account_processing.repository.AccountRepository;
import ru.t1.dto.KafkaMessageClientProduct;

import java.math.BigDecimal;

/**
 * Kafka-консьюмер для обработки сообщений из топика {@code client_products}.
 * Создаёт новый банковский счёт для клиента при получении операции CREATE.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerAccountService {
    private final AccountRepository accountRepository;

    /**
     * Обработка сообщений из Kafka-топика {@code client_products}.
     *
     * @param message сообщение о продукте клиента
     */
    @KafkaListener(topics = "client_products", groupId = "account-processing-group")
    public void consume(KafkaMessageClientProduct message) {
        log.info("Получено сообщение из Kafka: {}", message);

        if ("CREATE".equalsIgnoreCase(message.getOperation())) {
            Account account = new Account();
            account.setClientId(message.getClientId());
            account.setProductId(message.getProductId());
            account.setBalance(BigDecimal.ZERO);
            account.setInterestRate(BigDecimal.ZERO);
            account.setIsRecalc(false);
            account.setCardExist(false);
            account.setStatus(AccountStatus.ACTIVE);

            accountRepository.save(account);
            log.info("Создан новый Account для клиента {} и продукта {}",
                    message.getClientId(), message.getProductId());
        } else {
            log.info("Сообщение с операцией {} пока не обрабатывается", message.getOperation());
        }
    }
}
