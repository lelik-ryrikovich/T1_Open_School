package ru.t1.client_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.client_processing.entity.ClientProduct;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientProductRepository extends JpaRepository<ClientProduct, Long> {

    List<ClientProduct> findByClientId(Long clientId);

    @Query("SELECT cp FROM ClientProduct cp WHERE cp.client.id = :clientId AND cp.product.id = :productId")
    Optional<ClientProduct> findByClientIdAndProductId(@Param("clientId") Long clientId,
                                                       @Param("productId") Long productId);

    boolean existsByClientIdAndProductId(Long clientId, Long productId);

    @Query("SELECT cp FROM ClientProduct cp JOIN cp.product p WHERE cp.client.id = :clientId AND p.key IN :productKeys")
    List<ClientProduct> findByClientIdAndProductKeyIn(@Param("clientId") Long clientId,
                                                      @Param("productKeys") List<String> productKeys);
}