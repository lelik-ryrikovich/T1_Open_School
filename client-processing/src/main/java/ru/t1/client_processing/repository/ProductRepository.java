package ru.t1.client_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.aop.Cached;
import ru.t1.client_processing.entity.Product;
import ru.t1.client_processing.entity.enums.ProductKey;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Cached(cacheName = "Product")
    Optional<Product> findByProductId(String productId);

    boolean existsByProductId(String productId);

    boolean existsByName(String name);

    void deleteByProductId(String productId);
}