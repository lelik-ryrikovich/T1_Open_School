package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.service.PaymentService;
import ru.t1.dto.KafkaMessageClientPayment;

/**
 * Kafka-консьюмер для обработки сообщений о платежах из топика {@code client_payments}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientPaymentService {

    private final PaymentService paymentService;

    /**
     * Обработка входящего Kafka-сообщения о платеже.
     *
     * @param message сообщение с данными о платеже клиента
     */
    @KafkaListener(topics = "client_payments", groupId = "account-processing-group")
    public void consume(KafkaMessageClientPayment message) {
        log.info("Получено сообщение из топика client_payments: {}", message);
        try {
            paymentService.processPayment(message.getAccountId(), message.getAmount());
        } catch (Exception ex) {
            log.error("Ошибка обработки Kafka сообщения: {}", ex.getMessage(), ex);
        }
    }
}

