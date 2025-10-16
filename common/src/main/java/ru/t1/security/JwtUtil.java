package ru.t1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Утилита для работы с JWT токенами между микросервисами.
 * Используется для генерации и валидации токенов.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${app.service-name}")
    private String serviceName;

    @Value("${jwt.expiration-ms}") // 1 час по умолчанию
    private long expirationMs;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /** Генерирует JWT токен для межсервисного общения
     * @return JWT токен*/
    public String generateServiceToken() {
        return Jwts.builder()
                .setSubject(serviceName) // имя сервиса, который генерирует токен
                .claim("type", "SERVICE") // тип токена для межсервисного общения
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Проверяет валидность JWT токена
     * @param token JWT токен
     * @return если токен валиден*/
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Извлекает имя сервиса из JWT токена
     * @param token JWT токен
     * @return имя сервиса*/
    public String extractServiceName(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /** Извлекает тип токена из JWT токена
     * @param token JWT токен
     * @return тип токена
     **/
    public String extractTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("type", String.class);
    }

    /**
     * Декодирует и возвращает claims JWT токена (для логирования)
     */
    public Claims decodeToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Возвращает время истечения токена (для логирования)
     */
    public Date getExpirationDate(String token) {
        return decodeToken(token).getExpiration();
    }
}
