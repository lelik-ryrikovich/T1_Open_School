package ru.t1.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.starter.aop.HttpOutcomeRequestLogAspect;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(HttpOutcomeRequestLogAspect.class)
@Configuration
public class HttpOutcomeRequestLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HttpOutcomeRequestLogAspect httpOutcomeRequestLogAspect(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new HttpOutcomeRequestLogAspect(kafkaTemplate);
    }
}
