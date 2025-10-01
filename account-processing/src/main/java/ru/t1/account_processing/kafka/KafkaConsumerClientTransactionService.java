package ru.t1.account_processing.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka-консьюмер для обработки сообщений из топика {@code client_transactions}.
 * На данный момент реализован как заглушка — сообщения просто логируются.
 */
@Slf4j
@Service
public class KafkaConsumerClientTransactionService {

    /**
     * Обработка сообщений из Kafka-топика {@code client_transactions}.
     *
     * @param message сообщение о транзакции (в сыром виде)
     */
    @KafkaListener(topics = "client_transactions", groupId = "account-processing-group")
    public void consume(String message) {
        // Заглушка: просто выводим сообщение в лог
        log.info("Получено сообщение из топика client_transactions: {}", message);
    }
}
