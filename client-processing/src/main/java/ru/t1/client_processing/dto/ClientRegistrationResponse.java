package ru.t1.client_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegistrationResponse {
    private Long userId;           // ID созданного User
    private String login;          // Логин пользователя
    private String email;          // Email пользователя
    private String clientId;     // clientId в формате XXFFNNNNNNNN (для информации)
}