package ru.t1.client_processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.t1.config.KafkaTopicsConfig;

@SpringBootApplication
@Import(KafkaTopicsConfig.class)
public class ClientProcessingApp {
    public static void main(String args[]) {
        SpringApplication.run(ClientProcessingApp.class, args);
    }
}
