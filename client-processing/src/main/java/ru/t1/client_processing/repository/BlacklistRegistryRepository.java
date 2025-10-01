package ru.t1.client_processing.repository;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.t1.client_processing.entity.BlacklistRegistry;
import ru.t1.client_processing.entity.enums.DocumentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlacklistRegistryRepository extends JpaRepository<BlacklistRegistry, Long> {

    @Query("SELECT b FROM BlacklistRegistry b WHERE b.documentType = :documentType " +
            "AND b.documentId = :documentId " +
            "AND (b.blacklistExpirationAt IS NULL OR b.blacklistExpirationAt > :currentTime)")
    Optional<BlacklistRegistry> findActiveBlacklistEntry(
            @Param("documentType") DocumentType documentType,
            @Param("documentId") String documentId,
            @Param("currentTime") LocalDateTime currentTime);
}
