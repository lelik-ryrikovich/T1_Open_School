/*
package ru.t1.aop;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.entity.ErrorLog;
import ru.t1.entity.enums.LogType;
import ru.t1.repository.ErrorLogRepository;

import java.time.LocalDateTime;
import java.util.Arrays;

*/
/**
 * Аспект для перехвата ошибок, возникающих в методах,
 * помеченных аннотацией {@link LogDatasourceError}.
 *//*

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogDatasourceErrorAspect {
    */
/** Kafka-шаблон для публикации сообщений об ошибках. *//*

    private final KafkaTemplate<String, Object> kafkaTemplate;

    */
/** Репозиторий для сохранения ошибок в случае недоступности Kafka. *//*

    private final ErrorLogRepository errorLogRepository;

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
     * Перехватывает все исключения, выброшенные методами, аннотированными {@link LogDatasourceError},
     * и логирует их в Kafka или в базу данных при недоступности брокера.
     *
     * @param joinPoint точка соединения, содержащая информацию о методе и его аргументах
     * @param ex         перехваченное исключение
     *//*

    @AfterThrowing(pointcut = "@annotation(ru.t1.aop.LogDatasourceError)", throwing = "ex")
    public void logError(JoinPoint joinPoint, Throwable ex) {
        try {
            log.info("Аспект LogDatasourceErrorAspect перехватил исключение: {} в методе {}", ex.getMessage(), joinPoint.getSignature().toShortString());

            ErrorMessage errorMessage = new ErrorMessage(
                    LocalDateTime.now(),
                    joinPoint.getSignature().toShortString(),
                    ex.getMessage(),
                    Arrays.toString(ex.getStackTrace()),
                    Arrays.toString(joinPoint.getArgs())
            );

            // Определяем тип сообщения (INFO, WARNING, ERROR)
            String messageType = String.valueOf(determineMessageType(ex));

            // Формируем Kafka-сообщение
            Message<ErrorMessage> message = MessageBuilder
                    .withPayload(errorMessage)
                    .setHeader(KafkaHeaders.KEY, serviceName) // ключ сообщения = название микросервиса
                    .setHeader("type", messageType)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .build();

            kafkaTemplate.send(message).get();

            log.error("Отправлено сообщение об ошибке в Kafka из сервиса {}: {}",
                    message.getHeaders().get(KafkaHeaders.KEY), message.getPayload());

        } catch (Exception kafkaException) {
            log.error("Не удалось отправить сообщение в Kafka, сохраняем в БД", kafkaException);

            ErrorLog errorLog = new ErrorLog();
            errorLog.setTimestamp(LocalDateTime.now());
            errorLog.setMethodSignature(joinPoint.getSignature().toShortString());
            errorLog.setExceptionMessage(ex.getMessage());
            errorLog.setStackTrace(Arrays.toString(ex.getStackTrace()));
            errorLog.setMethodArgs(Arrays.toString(joinPoint.getArgs()));

            errorLogRepository.save(errorLog);
        }
    }

    */
/**
     * Внутренний DTO для передачи сообщений об ошибках в Kafka.
     *
     * @param timestamp       время возникновения ошибки
     * @param methodSignature сигнатура метода, в котором произошла ошибка
     * @param exceptionMessage сообщение об исключении
     * @param stackTrace      стек-трейс исключения
     * @param methodArgs      аргументы метода, в котором произошла ошибка
     *//*

    private record ErrorMessage(
            LocalDateTime timestamp,
            String methodSignature,
            String exceptionMessage,
            String stackTrace,
            String methodArgs
    ) {}

    */
/**
     * Определяет уровень важности (тип) ошибки на основе типа исключения.
     *
     * @param ex исключение, тип которого нужно определить
     * @return тип логирования {@link LogType}
     *//*

    @Enumerated(EnumType.STRING)
    private LogType determineMessageType(Throwable ex) {
        if (ex instanceof NullPointerException ||
                ex instanceof IllegalArgumentException) {
            return LogType.WARNING;
        } else if (ex instanceof RuntimeException) {
            return LogType.ERROR;
        } else {
            return LogType.INFO;
        }
    }
}
*/
