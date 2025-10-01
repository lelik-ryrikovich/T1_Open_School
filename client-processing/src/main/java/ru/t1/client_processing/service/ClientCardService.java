package ru.t1.client_processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.client_processing.kafka.KafkaProducerService;
import ru.t1.dto.KafkaMessageClientCard;

/**
 * Сервис для работы с картами клиента.
 * Отвечает за отправку событий о создании карты в Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientCardService {
    private final KafkaProducerService kafkaProducerService;

    @Value("${app.kafka.topics.client-cards}")
    private String clientCardsTopic;

    /**
     * Отправка запроса на создание карты в Kafka.
     *
     * @param request сообщение с данными о карте клиента
     */
    public void sendCardCreateRequest(KafkaMessageClientCard request) {
        log.info("Отправка запроса на создание карты в Kafka: {}", request);
        request.setOperation("CREATE");
        kafkaProducerService.sendMessage(clientCardsTopic, request);
    }

}
