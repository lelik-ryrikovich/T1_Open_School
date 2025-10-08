package ru.t1.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Конфигурация Kafka-топиков.
 *
 * Все топики создаются автоматически при старте приложения, если они отсутствуют
 * в брокере Kafka. Для этого используется {@link NewTopic}, который управляется
 * Spring Kafka AdminClient.
 *
 * Каждый топик создаётся с 3 партициями и фактором репликации 1.
 */
@Configuration
public class KafkaTopicsConfig {
    /**
     * Топик для событий, связанных с клиентскими продуктами.
     *
     * @return объект топика {@link NewTopic}
     */
    @Bean
    public NewTopic clientProductsTopic() {
        return TopicBuilder.name("client_products")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Топик для событий, связанных с кредитными продуктами клиентов.
     *
     * @return объект топика {@link NewTopic}
     */
    @Bean
    public NewTopic clientCreditProductsTopic() {
        return TopicBuilder.name("client_credit_products")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Топик для событий, связанных с картами клиентов.
     *
     * @return объект топика {@link NewTopic}
     */
    @Bean
    public NewTopic clientCardsTopic() {
        return TopicBuilder.name("client_cards")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Топик для событий, связанных с транзакциями клиентов.
     *
     * @return объект топика {@link NewTopic}
     */
    @Bean
    public NewTopic clientTransactionsTopic() {
        return TopicBuilder.name("client_transactions")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Топик для логов программы.
     *
     * @return объект топика {@link NewTopic}
     */
    @Bean
    public NewTopic serviceLogsTopic() {
        return TopicBuilder.name("service_logs")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
