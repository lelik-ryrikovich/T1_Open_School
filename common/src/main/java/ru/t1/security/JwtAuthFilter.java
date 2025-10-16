package ru.t1.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

/**
 * Фильтр для проверки JWT токенов во входящих HTTP запросах.
 * Устанавливается в микросервисах, которые принимают запросы от других сервисов.
 * */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);

        if (jwt != null && jwtUtil.validateToken(jwt)) {
            logIncomingJwt(jwt, request);
            String serviceName = jwtUtil.extractServiceName(jwt);
            String tokenType = jwtUtil.extractTokenType(jwt);

            if ("SERVICE".equals(tokenType)) {
                log.info("✅ JWT token validated. Service: {}", serviceName);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        serviceName,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                log.info("❌ Invalid token type: {}", tokenType);
                // НЕ блокируем запрос, просто пропускаем аутентификацию
            }
        } else if (jwt != null) {
            log.info("❌ Invalid JWT token");
            // НЕ блокируем запрос, просто пропускаем аутентификацию
        } else {
            log.info("🔓 No JWT token - allowing request as anonymous");
            // НЕ блокируем запрос - разрешаем как анонимный
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        return (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer "))
                ? headerAuth.substring(7)
                : null;
    }

    /**
     * Логирует информацию о входящем JWT токене
     */
    private void logIncomingJwt(String jwtToken, HttpServletRequest request) {
        try {
            Claims claims = jwtUtil.decodeToken(jwtToken);
            String serviceName = claims.getSubject();
            String tokenType = claims.get("type", String.class);
            Date expiration = claims.getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();

            log.info("📨 Incoming JWT from: {}, Service: {}, Type: {}",
                    request.getRemoteAddr(), serviceName, tokenType);
            log.debug("⏰ JWT Expires: {} (in {} minutes)",
                    expiration, remainingMs / 60000);
            log.debug("📋 JWT Claims: {}", claims);

        } catch (Exception e) {
            log.warn("⚠️ Failed to decode incoming JWT: {}", e.getMessage());
        }
    }
}
