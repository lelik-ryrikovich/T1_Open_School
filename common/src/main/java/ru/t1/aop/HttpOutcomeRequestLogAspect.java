/*
package ru.t1.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

*/
/**
 * AOP-аспект для логирования исходящих HTTP-запросов.
 *
 * <p>Аспект перехватывает методы, аннотированные {@link HttpOutcomeRequestLog},
 * и выполняет логирование информации об исходящем HTTP-запросе и его ответе.
 * Данные публикуются в Kafka-топик.</p>
 *//*

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HttpOutcomeRequestLogAspect {
    */
/** Kafka-шаблон для отправки сообщений о внешних HTTP-запросах. *//*

    private final KafkaTemplate<String, Object> kafkaTemplate;

    */
/** Название Kafka-топика для логов сервисов. *//*

    @Value("${app.kafka.topics.service-logs}")
    private String topic;

    */
/** Имя текущего сервиса (используется как ключ Kafka-сообщения). *//*

    @Value("${app.service-name}")
    private String serviceName;

    */
/**
     * Логирует результат выполнения метода, аннотированного {@link HttpOutcomeRequestLog}.
     *
     * <p>Метод вызывается после успешного завершения метода-клиента (например, REST-запроса),
     * формирует структуру лог-сообщения и отправляет её в Kafka.</p>
     *
     * @param joinPoint контекст вызова метода (метаданные о методе и его аргументах)
     * @param response  объект, возвращаемый методом (ответ внешнего сервиса)
     *//*

    @AfterReturning(pointcut = "@annotation(ru.t1.aop.HttpOutcomeRequestLog)", returning = "response")
    public void logHttpRequest(JoinPoint joinPoint, Object response) {
        try {
            Object[] args = joinPoint.getArgs();

            // Формируем сообщение для Kafka
            HttpOutcomeLogMessage logMessage = new HttpOutcomeLogMessage(
                    LocalDateTime.now(),
                    joinPoint.getSignature().toShortString(),
                    extractUri(args),
                    extractParams(args),
                    response
            );

            // Подготавливаем и отправляем сообщение в Kafka
            Message<HttpOutcomeLogMessage> message = MessageBuilder
                    .withPayload(logMessage)
                    .setHeader(KafkaHeaders.KEY, serviceName)
                    .setHeader("type", "INFO")
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .build();

            kafkaTemplate.send(message).get();

            log.info("Отправлено лог-сообщение о HTTP запросе: {}", logMessage);

        } catch (Exception e) {
            log.error("Ошибка при отправке лог-сообщения о HTTP запросе", e);
        }
    }

    */
/**
     * Извлекает URI из аргументов метода.
     *
     * <p>Метод ищет первый аргумент типа {@link String},
     * который начинается с "http" — предполагается, что это адрес внешнего сервиса.</p>
     *
     * @param args массив аргументов метода
     * @return URI внешнего запроса или {@code null}, если не найден
     *//*

    private String extractUri(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String uri && uri.startsWith("http")) {
                return uri;
            }
        }
        return null;
    }

    */
/**
     * Извлекает параметры запроса из аргументов метода.
     *
     * <p>Метод ищет первый аргумент, являющийся объектом {@link Map},
     * и возвращает его как набор параметров исходящего запроса.</p>
     *
     * @param args массив аргументов метода
     * @return карта параметров или {@code null}, если не найдена
     *//*

    private Map<String, Object> extractParams(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        }
        return null;
    }

    */
/**
     * DTO для Kafka-сообщений о исходящих HTTP-запросах.
     * Используется как полезная нагрузка лог-сообщения.
     *
     * @param timestamp       время получения запроса
     * @param methodSignature сигнатура вызываемого метода
     * @param uri             адрес исходящего HTTP-запроса
     * @param params          карта параметров, переданных во внешний запрос
     * @param body            тело ответа, возвращённое внешним сервисом
     *//*

    private record HttpOutcomeLogMessage(
            LocalDateTime timestamp,
            String methodSignature,
            String uri,
            Map<String, Object> params,
            Object body
    ) {}
}
*/
