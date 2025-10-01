package ru.t1.client_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность пользователя системы (учетные данные).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный логин пользователя. */
    @Column(unique = true, nullable = false)
    private String login;

    /** Пароль пользователя. */
    @Column(nullable = false)
    private String password;

    /** Уникальный email пользователя. */
    @Column(unique = true, nullable = false)
    private String email;
}
