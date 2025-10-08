package ru.t1.account_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.account_processing.service.CardService;
import ru.t1.dto.KafkaMessageClientCard;

/**
 * Kafka-консьюмер для обработки сообщений из топика {@code client_cards}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerClientCardService {
    private final CardService cardService;

    /**
     * Обработка сообщений из Kafka-топика {@code client_cards}.
     *
     * @param message сообщение о банковской карте клиента
     */
    @KafkaListener(topics = "client_cards", groupId = "account-processing-group")
    public void consume(KafkaMessageClientCard message) {
        log.info("Получено сообщение из топика client_cards: {}", message);
        try {
            if ("CREATE".equalsIgnoreCase(message.getOperation())) {
                cardService.createCard(message.getAccountId(), message.getClientId(), message.getPaymentSystem());
            } else {
                log.info("Операция {} пока не поддерживается", message.getOperation());
            }
        } catch (Exception ex) {
            log.error("Ошибка обработки Kafka сообщения: {}", ex.getMessage());
        }
    }
}
