package ru.t1.client_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.client_processing.entity.enums.ProductKey;
import ru.t1.client_processing.entity.enums.ProductStatus;
import ru.t1.starter.aop.annotation.Cached;
import ru.t1.client_processing.entity.ClientProduct;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientProductRepository extends JpaRepository<ClientProduct, Long> {
    @Cached(cacheName = "Client")
    List<ClientProduct> findByClientId(Long clientId);

    boolean existsByClientIdAndProductId(Long clientId, Long productId);

    @Query("SELECT COUNT(cp) FROM ClientProduct cp WHERE cp.product.key = :productKey AND cp.status = :status")
    long countByProductKeyAndStatus(@Param("productKey") ProductKey productKey,
                                    @Param("status") ProductStatus status);

}