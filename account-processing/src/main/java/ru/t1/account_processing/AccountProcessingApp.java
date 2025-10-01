package ru.t1.account_processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.t1.config.KafkaTopicsConfig;

@SpringBootApplication
@Import(KafkaTopicsConfig.class)
public class AccountProcessingApp {
    public static void main(String[] args) {
        SpringApplication.run(AccountProcessingApp.class, args);
    }
}
