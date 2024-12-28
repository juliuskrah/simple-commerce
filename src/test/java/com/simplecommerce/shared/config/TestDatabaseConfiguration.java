package com.simplecommerce.shared.config;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestDatabaseConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>("postgres:17.2-alpine")
        .withMinimumRunningDuration(Duration.ofSeconds(5L));
  }
}
