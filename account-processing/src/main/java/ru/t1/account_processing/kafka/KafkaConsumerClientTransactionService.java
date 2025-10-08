package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.service.TransactionService;
import ru.t1.dto.KafkaMessageClientTransaction;

/**
 * Kafka-консьюмер для обработки клиентских транзакций из топика {@code client_transactions}.
 * Делегирует бизнес-логику в {@link TransactionService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientTransactionService {

    private final TransactionService transactionService;

    /**
     * Обрабатывает входящее сообщение о транзакции.
     *
     * @param message сообщение с данными о транзакции
     */
    @KafkaListener(topics = "client_transactions", groupId = "account-processing-group")
    public void consume(KafkaMessageClientTransaction message) {
        log.info("Получено сообщение из топика client_transactions: {}", message);
        try {
            transactionService.processTransaction(
                    message.getAccountId(),
                    message.getCardId(),
                    message.getType(),
                    message.getAmount()
            );
        } catch (Exception ex) {
            log.error("Ошибка обработки Kafka сообщения: {}", ex.getMessage(), ex);
        }
    }
}