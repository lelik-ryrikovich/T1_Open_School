package ru.t1.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.t1.starter.aop.HttpIncomeRequestLogAspect;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(HttpIncomeRequestLogAspect.class)
@Configuration
public class HttpIncomeRequestLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HttpIncomeRequestLogAspect httpIncomeRequestLogAspect(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping
    ) {
        return new HttpIncomeRequestLogAspect(kafkaTemplate, handlerMapping);
    }
}