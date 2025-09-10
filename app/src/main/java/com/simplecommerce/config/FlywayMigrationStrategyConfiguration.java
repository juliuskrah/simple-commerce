package com.simplecommerce.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author julius.krah
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class FlywayMigrationStrategyConfiguration {
    
    @Bean
    @ConditionalOnBooleanProperty(name = "spring.flyway.enabled")
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Do not migrate automatically
        };
    }
}
