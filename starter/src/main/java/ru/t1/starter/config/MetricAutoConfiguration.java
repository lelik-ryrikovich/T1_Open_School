package ru.t1.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.starter.aop.MetricAspect;

@AutoConfiguration
@Configuration
@ConditionalOnClass(MetricAspect.class)
public class MetricAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MetricAspect metricAspect(KafkaTemplate<String, Object> kafkaTemplate) {
        return new MetricAspect(kafkaTemplate);
    }
}
