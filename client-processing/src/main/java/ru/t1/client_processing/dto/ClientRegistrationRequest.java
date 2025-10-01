package ru.t1.client_processing.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientRegistrationRequest {
    private String login;
    private String password;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String documentType;
    private String documentId;
    private String documentPrefix;
    private String documentSuffix;
}
