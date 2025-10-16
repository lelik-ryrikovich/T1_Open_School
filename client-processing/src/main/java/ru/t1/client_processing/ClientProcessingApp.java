package ru.t1.client_processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.t1.config.KafkaTopicsConfig;
import ru.t1.security.CommonSecurityConfig;

@SpringBootApplication(scanBasePackages = "ru.t1")
//@SpringBootApplication
//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
//@Import({KafkaTopicsConfig.class, CommonSecurityConfig.class})
@Import(KafkaTopicsConfig.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = "ru.t1.client_processing.repository")
@EntityScan(basePackages = "ru.t1.client_processing.entity")
//@ComponentScan(basePackages = {"ru.t1.security", "ru.t1.client"})
//@EnableConfigurationProperties

public class ClientProcessingApp {
    public static void main(String[] args) {
        SpringApplication.run(ClientProcessingApp.class, args);
    }
}
