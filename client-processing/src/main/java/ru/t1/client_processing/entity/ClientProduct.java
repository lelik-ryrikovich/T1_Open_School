package ru.t1.client_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.client_processing.entity.enums.ProductStatus;

import java.time.LocalDateTime;

/**
 * Сущность для связи клиента и его продуктов (счета, кредиты, карты и т.п.).
 */
@Entity
@Table(name = "client_products")
@Getter
@Setter
public class ClientProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Клиент, владеющий продуктом. */
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /** Банковский продукт. */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Дата открытия продукта. */
    @Column(name = "open_date", nullable = false)
    private LocalDateTime openDate;

    /** Дата закрытия продукта. */
    @Column(name = "close_date")
    private LocalDateTime closeDate;

    /** Текущий статус продукта. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
}
