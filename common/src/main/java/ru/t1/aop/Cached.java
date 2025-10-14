/*
package ru.t1.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cached {
    */
/**
     * Имя кэша (если нужно разделять хранилища по типам данных).
     * Можно не указывать — тогда используется общий кэш.
     *//*

    String cacheName() default "";

    */
/**
     * Время жизни записи (в миллисекундах).
     * Если не указано, берётся глобальное значение из application.yml.
     *//*

    long ttl() default -1;
}
*/
