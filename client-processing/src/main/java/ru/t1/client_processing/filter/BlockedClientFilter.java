package ru.t1.client_processing.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.t1.client_processing.service.ClientBlockService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockedClientFilter extends OncePerRequestFilter {

    private final ClientBlockService clientBlockService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Сначала проверяем Basic Auth header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length());
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);

                if (values.length == 2) {
                    String username = values[0];
                    log.info("🔐 Checking Basic Auth user: {}", username);

                    boolean isBlocked = clientBlockService.isUserBlockedByUsername(username);
                    if (isBlocked) {
                        log.warn("🚫 Blocked Basic Auth user attempted to access: {} {} {}",
                                username, request.getMethod(), request.getRequestURI());

                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Client is blocked\"}");
                        return;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to decode Basic Auth header: {}", e.getMessage());
            }
        }

        // 2. Затем проверяем Spring Security аутентификацию
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {

            String username = authentication.getName();
            log.info("🔐 Checking Spring Auth user: {}", username);

            boolean isBlocked = clientBlockService.isUserBlockedByUsername(username);
            if (isBlocked) {
                log.warn("🚫 Blocked Spring Auth user attempted to access: {} {} {}",
                        username, request.getMethod(), request.getRequestURI());

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Client is blocked\"}");
                return;
            }
        }

        // 3. Если не заблокирован или нет Basic Auth - продолжаем
        filterChain.doFilter(request, response);
    }
}