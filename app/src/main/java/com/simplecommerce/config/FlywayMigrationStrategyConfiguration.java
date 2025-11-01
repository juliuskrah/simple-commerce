package com.simplecommerce.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * @author julius.krah
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(name = "spring.flyway.enabled")
public class FlywayMigrationStrategyConfiguration {
    
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(Environment environment) {
        return flyway -> {
          if (environment.acceptsProfiles(Profiles.of("clean", "seed"))) {
            flyway.clean();
          }
        };
    }

  @Bean
  ApplicationRunner migrateFlyway(Flyway flyway) {
    return _ -> flyway.migrate();
  }
}
