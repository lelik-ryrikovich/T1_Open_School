package ru.t1.client_processing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.client_processing.entity.enums.DocumentType;

import java.time.LocalDate;

/**
 * Сущность клиента (физического лица).
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный идентификатор клиента (формат: XXFFNNNNNNNN). */
    @Column(name = "client_id", unique = true, nullable = false, length = 12)
    private String clientId;

    /** Пользовательские учетные данные, связанные с клиентом. */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Дата рождения клиента. */
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /** Тип документа. */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    /** Номер документа клиента. */
    @Column(name = "document_id", nullable = false)
    private String documentId;

    /** Префикс документа. */
    @Column(name = "document_prefix")
    private String documentPrefix;

    /** Суффикс документа. */
    @Column(name = "document_suffix")
    private String documentSuffix;
}
