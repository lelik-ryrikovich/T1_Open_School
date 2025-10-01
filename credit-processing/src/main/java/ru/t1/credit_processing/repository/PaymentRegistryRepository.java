package ru.t1.credit_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.credit_processing.entity.PaymentRegistry;

import java.util.List;

@Repository
public interface PaymentRegistryRepository extends JpaRepository<PaymentRegistry, Long> {

    // ищем хотя бы одну просрочку по всем продуктам клиента
    @Query("SELECT COUNT(payReg) > 0 FROM PaymentRegistry payReg " +
            "JOIN ProductRegistry prodReg ON payReg.productRegistry.productId = prodReg.productId " +
            "WHERE prodReg.clientId = :clientId AND payReg.expired = true")
    boolean existsExpiredPaymentsByClientId(@Param("clientId") Long clientId);

}

