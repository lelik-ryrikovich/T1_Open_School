package ru.t1.dto;

import lombok.Data;

@Data
public class ClientInfoResponse {
    private String firstName;
    private String middleName;
    private String lastName;
    private String documentType;
    private String documentId;
}


