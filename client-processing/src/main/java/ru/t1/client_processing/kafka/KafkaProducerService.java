package ru.t1.client_processing.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки сообщений в Kafka.
 * Оборачивает {@link KafkaTemplate}, добавляя логирование и обработку ошибок.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Отправка сообщения в указанный Kafka-топик.
     *
     * @param topic   название топика
     * @param message объект-сообщение (будет сериализован)
     */
    public void sendMessage(String topic, Object message) {
        if (topic != null) {
            kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Ошибка при отправке сообщения в Kafka topic {}", topic, ex);
                        } else {
                            log.info("Сообщение отправлено в Kafka topic {}: {}", topic, message);
                        }
                    });
        } else {
            log.error("Топик не определен (null)");
        }
    }
}
