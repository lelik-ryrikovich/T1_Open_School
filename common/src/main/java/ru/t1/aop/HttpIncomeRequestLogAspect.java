/*
package ru.t1.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * AOP-аспект для логирования входящих HTTP-запросов.
 *
 * <p>Аспект перехватывает методы, аннотированные {@link ru.t1.aop.HttpIncomeRequestLog},
 * извлекает информацию о запросе (URI, HTTP-метод, параметры, тело)
 * и публикует событие в Kafka-топик.</p>
 *
 * <p>Каждое сообщение включает:
 * <ul>
 *     <li>время запроса,</li>
 *     <li>сигнатуру метода,</li>
 *     <li>URI и HTTP-метод,</li>
 *     <li>параметры пути и запроса,</li>
 *     <li>тело запроса.</li>
 * </ul>
 * </p>
 *//*

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HttpIncomeRequestLogAspect {
    */
/** Kafka-шаблон для отправки сообщений о входящих запросах. *//*

    private final KafkaTemplate<String, Object> kafkaTemplate;

    */
/** Маппинг обработчиков Spring MVC для определения метода контроллера. *//*

    private final RequestMappingHandlerMapping handlerMapping;

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
     * Аспект, выполняющий логирование перед выполнением любого метода,
     * аннотированного {@link ru.t1.aop.HttpIncomeRequestLog}.
     *
     * <p>Собирает информацию о текущем HTTP-запросе (URI, параметры, тело)
     * и публикует JSON-сообщение в Kafka.</p>
     *
     * @param joinPoint контекст выполняемого метода (переданный Spring AOP)
     *//*

    @Before("@annotation(ru.t1.aop.HttpIncomeRequestLog)")
    public void logIncomingRequest(JoinPoint joinPoint) {
        try {
            // Извлекаем текущий HTTP-запрос
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                            .getRequest();

            // Определяем контроллерный метод, обрабатывающий этот запрос
            HandlerMethod handlerMethod =
                    (HandlerMethod) handlerMapping.getHandler(request).getHandler();

            Parameter[] parameters = handlerMethod.getMethod().getParameters();
            Object[] args = joinPoint.getArgs();

            Map<String, Object> pathAndQueryParams = new HashMap<>();
            Object body = null;

            // Извлекаем параметры пути, запроса и тело
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Object value = args[i];

                if (param.isAnnotationPresent(org.springframework.web.bind.annotation.PathVariable.class)) {
                    String name = param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class).value();
                    pathAndQueryParams.put(name.isEmpty() ? param.getName() : name, value);
                } else if (param.isAnnotationPresent(org.springframework.web.bind.annotation.RequestParam.class)) {
                    String name = param.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class).value();
                    pathAndQueryParams.put(name.isEmpty() ? param.getName() : name, value);
                } else if (param.isAnnotationPresent(org.springframework.web.bind.annotation.RequestBody.class)) {
                    body = value;
                }
            }

            // Формируем сообщение для Kafka
            HttpIncomeLogMessage logMessage = new HttpIncomeLogMessage(
                    LocalDateTime.now(),
                    joinPoint.getSignature().toShortString(),
                    request.getRequestURI(),
                    request.getMethod(),
                    pathAndQueryParams,
                    body
            );

            // Подготавливаем и отправляем сообщение в Kafka
            Message<HttpIncomeLogMessage> message = MessageBuilder
                    .withPayload(logMessage)
                    .setHeader(KafkaHeaders.KEY, serviceName)
                    .setHeader("type", "INFO")
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .build();

            kafkaTemplate.send(message).get();
            log.info("Входящий HTTP-запрос залогирован: {}", logMessage);
        } catch (Exception e) {
            log.error("Ошибка при логировании входящего HTTP-запроса", e);
        }
    }

    */
/**
     * DTO для логирования входящих HTTP-запросов.
     *
     * @param timestamp время получения запроса
     * @param methodSignature сигнатура вызываемого метода
     * @param uri URI запроса
     * @param httpMethod HTTP-метод (GET, POST и т.п.)
     * @param params карта параметров пути и запроса
     * @param body тело запроса
     *//*

    private record HttpIncomeLogMessage(
            LocalDateTime timestamp,
            String methodSignature,
            String uri,
            String httpMethod,
            Map<String, Object> params,
            Object body
    ) {}
}
*/
