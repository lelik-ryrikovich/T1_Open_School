package ru.t1.client;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.t1.security.JwtUtil;
import ru.t1.starter.aop.annotation.HttpOutcomeRequestLog;
import ru.t1.dto.ClientInfoResponse;
import ru.t1.dto.ProductRegistryInfo;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Универсальный HTTP-клиент для взаимодействия между микросервисами.
 * <p>
 * Обеспечивает выполнение REST-запросов (в основном GET) к другим сервисам
 * с использованием {@link RestTemplate}.
 * <p>
 * Все методы аннотированы {@link HttpOutcomeRequestLog}, что позволяет
 * автоматически логировать исходящие HTTP-запросы (через AOP-аспект).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessingHttpClient {
    private final RestTemplate restTemplate = new RestTemplate();

    private final JwtUtil jwtUtil;

    // Создаем HTTP заголовки с JWT токеном
    private HttpHeaders createHeadersWithJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Генерируем JWT токен для межсервисного вызова
        String jwtToken = jwtUtil.generateServiceToken();
        headers.set("Authorization", "Bearer " + jwtToken);

        // Логируем JWT токен
        logJwtToken(jwtToken);

        return headers;
    }

    /**
     * Безопасное логирование JWT токена
     */
    private void logJwtToken(String jwtToken) {
        try {
            // Декодируем JWT для просмотра payload
            String[] parts = jwtToken.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                log.info("🔐 Generated JWT Token for service: {}", jwtUtil.extractServiceName(jwtToken));
                log.info("📋 JWT Payload: {}", payload);
                log.info("🔑 JWT Full Token: Bearer {}", jwtToken);

                // Логируем expiration time
                Date expiration = Jwts.parserBuilder()
                        .setSigningKey(jwtUtil.getSigningKey())
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getBody()
                        .getExpiration();
                log.info("⏰ JWT Expires at: {}", expiration);
            }
        } catch (Exception e) {
            log.info("⚠️ Failed to decode JWT for logging: {}", e.getMessage());
        }
    }

    @HttpOutcomeRequestLog
    public Long sendGetAccountIdRequest(String url, Map<String, Object> params) {
        HttpEntity<String> entity = new HttpEntity<>(createHeadersWithJwt());

        ResponseEntity<Long> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Long.class, params
        );
        return response.getBody();
    }

    @HttpOutcomeRequestLog
    public ClientInfoResponse sendGetClientInfoRequest(String url, Map<String, Object> params) {
        HttpEntity<String> entity = new HttpEntity<>(createHeadersWithJwt());

        ResponseEntity<ClientInfoResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, ClientInfoResponse.class, params
        );
        return response.getBody();
    }

    @HttpOutcomeRequestLog
    public ProductRegistryInfo sendGetProductRegistryByAccountRequest(String url, Map<String, Object> params) {
        HttpEntity<String> entity = new HttpEntity<>(createHeadersWithJwt());

        ResponseEntity<ProductRegistryInfo> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, ProductRegistryInfo.class, params
        );
        return response.getBody();
    }
}
