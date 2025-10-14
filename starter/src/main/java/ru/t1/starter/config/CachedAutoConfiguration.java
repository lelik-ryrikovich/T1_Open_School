package ru.t1.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.starter.aop.CachedAspect;

@AutoConfiguration
@Configuration
@ConditionalOnClass(CachedAspect.class)
public class CachedAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public CachedAspect cachedAspect() {
        return new CachedAspect();
    }
}