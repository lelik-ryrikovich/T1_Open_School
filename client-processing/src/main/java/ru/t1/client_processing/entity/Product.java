package ru.t1.client_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.client_processing.entity.enums.ProductKey;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductKey key;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "product_id", unique = true)
    private String productId;

    @PostPersist
    public void generateProductId() {
        this.productId = key.name() + id;
    }
}
