package ru.t1.client_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.client_processing.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("SELECT MAX(c.clientId) FROM Client c WHERE c.clientId LIKE :prefix%")
    String findMaxClientIdByPrefix(@Param("prefix") String prefix);
}
