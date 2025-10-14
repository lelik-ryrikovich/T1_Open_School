package ru.t1.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.starter.aop.LogDatasourceErrorAspect;
import ru.t1.starter.repository.ErrorLogRepository;

@AutoConfiguration
@Configuration
@EnableJpaRepositories(basePackages = "ru.t1.starter.repository")
@EntityScan(basePackages = "ru.t1.starter.entity")
@ConditionalOnClass(LogDatasourceErrorAspect.class)
public class LogDatasourceErrorAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public LogDatasourceErrorAspect logDatasourceErrorAspect(
            KafkaTemplate<String, Object> kafkaTemplate,
            ErrorLogRepository errorLogRepository
    ) {
        return new LogDatasourceErrorAspect(kafkaTemplate, errorLogRepository);
    }
}
