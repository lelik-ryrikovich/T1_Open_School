package ru.t1.client_processing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.t1.client_processing.filter.BlockedClientFilter;
import ru.t1.client_processing.repository.UserRepository;

import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.info("🔐 Loading user: {}", username);
            return userRepository.findByLogin(username)
                    .map(user -> {
                        log.info("✅ User found: {}, roles: {}", user.getLogin(),
                                user.getRoles().stream()
                                        .map(role -> role.getName().name())
                                        .collect(Collectors.toList()));

                        return User.builder()
                                .username(user.getLogin())
                                .password("{noop}" + user.getPassword())
                                .roles(user.getRoles().stream()
                                        .map(role -> role.getName().name().replace("ROLE_", ""))
                                        .toArray(String[]::new))
                                .build();
                    })
                    .orElseThrow(() -> {
                        log.info("❌ User not found: {}", username);
                        return new RuntimeException("User not found: " + username);
                    });
        };
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                          BlockedClientFilter blockedClientFilter) throws Exception {
        http
                .securityMatcher(
                        "/api/products/create",
                        "/api/products/update/**",
                        "/api/products/delete/**",
                        "/api/admin/**"
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})
                // Добавляем фильтр, но он будет работать ТОЛЬКО если есть Basic Auth
                .addFilterBefore(blockedClientFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}