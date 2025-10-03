package ru.t1.credit_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.credit_processing.entity.ProductRegistry;

import java.util.List;

@Repository
public interface ProductRegistryRepository extends JpaRepository<ProductRegistry, Long> {
    List<ProductRegistry> findByClientId(Long clientId);

    ProductRegistry findByAccountId(Long accountId);
}
