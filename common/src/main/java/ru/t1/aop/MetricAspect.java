package ru.t1.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Название Kafka-топика для логов сервисов. */
    @Value("${app.kafka.topics.service-logs}")
    private String topic;

    /** Имя микросервиса, используется как Kafka key. */
    @Value("${app.service-name}")
    private String serviceName;

    /** Лимит времени выполнения метода (в миллисекундах). */
    @Value("${metric.execution-limit-ms}")
    private long executionLimitMs;

    /**
     * Измеряет время выполнения метода, аннотированного {@link Metric}.
     * Если превышен лимит {@code executionLimitMs}, сообщение отправляется в Kafka.
     *
     * @param joinPoint точка соединения с методом
     * @return результат выполнения метода
     * @throws Throwable пробрасывает исключение, если метод выбросил его
     */
    @Around("@annotation(ru.t1.aop.Metric)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        log.info("Метод {} начинает выполняться", joinPoint.getSignature().getName());

        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            if (durationMs > executionLimitMs) {
                log.warn("Метод {} выполнялся {} мс (превышен лимит {} мс)",
                        joinPoint.getSignature(), durationMs, executionLimitMs);

                MetricMessage messagePayload = new MetricMessage(
                        LocalDateTime.now(),
                        joinPoint.getSignature().toShortString(),
                        durationMs,
                        Arrays.toString(joinPoint.getArgs())
                );

                Message<MetricMessage> message = MessageBuilder
                        .withPayload(messagePayload)
                        .setHeader(KafkaHeaders.KEY, serviceName)
                        .setHeader("type", "WARNING")
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build();

                try {
                    kafkaTemplate.send(message).get();
                    log.info("Отправлено предупреждение о медленном методе в Kafka: {}", messagePayload);
                } catch (Exception e) {
                    log.error("Ошибка при отправке метрики в Kafka", e);
                }
            }
        }
    }

    /**
     * DTO для Kafka-сообщений о превышении лимита выполнения метода.
     *
     * @param timestamp время регистрации события
     * @param methodSignature сигнатура метода
     * @param executionTimeMs время выполнения в миллисекундах
     * @param args аргументы метода в виде строки
     */
    private record MetricMessage(
            LocalDateTime timestamp,
            String methodSignature,
            long executionTimeMs,
            String args
    ) {}
}
