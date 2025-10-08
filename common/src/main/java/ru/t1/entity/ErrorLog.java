package ru.t1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Лог ошибок, сохраняемый в БД, если не удалось отправить сообщение в Kafka топик service_logs.
 */
@Entity
@Table(name = "error_log")
@Getter
@Setter
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Время возникновения ошибки */
    private LocalDateTime timestamp;

    /** Сигнатура метода, где произошла ошибка */
    private String methodSignature;

    /** Текст ошибки */
    @Column(length = 2000)
    private String exceptionMessage;

    /** Стек вызова */
    @Column(length = 8000)
    private String stackTrace;

    /** Входные параметры метода */
    @Column(length = 2000)
    private String methodArgs;
}
