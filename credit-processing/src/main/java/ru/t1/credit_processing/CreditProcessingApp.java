package ru.t1.credit_processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.t1.config.KafkaTopicsConfig;

@SpringBootApplication(scanBasePackages = "ru.t1")
@Import(KafkaTopicsConfig.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = "ru.t1.credit_processing.repository")
@EntityScan(basePackages = "ru.t1.credit_processing.entity")
public class CreditProcessingApp {
    public static void main(String[] args) {
        SpringApplication.run(CreditProcessingApp.class, args);
    }
}