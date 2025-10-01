package ru.t1.client_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.client_processing.entity.enums.DocumentType;

import java.time.LocalDateTime;

/**
 * Сущность для хранения информации о заблокированных клиентах
 * (черный список).
 */
@Entity
@Table(name = "blacklist_registry")
@Getter
@Setter
public class BlacklistRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Тип документа клиента (паспорт, СНИЛС и т.д.). */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    /** Номер документа клиента. */
    @Column(name = "document_id", nullable = false)
    private String documentId;

    /** Дата и время добавления в черный список. */
    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;

    /** Причина блокировки клиента. */
    @Column(nullable = false)
    private String reason;

    /**
     * Дата окончания действия блокировки.
     * Null = бессрочная блокировка.
     */
    @Column(name = "blacklist_expiration_date")
    private LocalDateTime blacklistExpirationAt;
}
