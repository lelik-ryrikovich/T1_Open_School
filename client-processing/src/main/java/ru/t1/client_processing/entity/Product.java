package ru.t1.client_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.client_processing.entity.enums.ProductKey;

import java.time.LocalDateTime;

/**
 * Сущность банковского продукта (депозит, кредит, карта и т.п.).
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название продукта. */
    @Column(nullable = false)
    private String name;

    /** Ключ продукта. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductKey key;

    /** Дата создания продукта. */
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    /** Уникальный идентификатор продукта. */
    @Column(name = "product_id", unique = true)
    private String productId;

    /** Генерация productId после сохранения (key + id). */
    @PostPersist
    public void generateProductId() {
        this.productId = key.name() + id;
    }
}
