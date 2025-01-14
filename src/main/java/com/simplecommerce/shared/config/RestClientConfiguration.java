package com.simplecommerce.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
public class RestClientConfiguration {

  @Bean
  RestClient restClient(Builder builder) {
    return builder.build();
  }
}
