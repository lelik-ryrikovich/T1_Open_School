package ru.t1.starter.aop.annotation;

import java.lang.annotation.*;

/**
 * Аннотация для логирования ошибок при работе с базой данных.
 * При возникновении исключения метод, помеченный этой аннотацией,
 * будет перехвачен аспектом, и ошибка будет зафиксирована
 * в Kafka (топик service_logs) или в таблице error_log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogDatasourceError {
}

