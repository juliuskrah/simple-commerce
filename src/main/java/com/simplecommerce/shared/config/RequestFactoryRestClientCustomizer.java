package com.simplecommerce.shared.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.Builder;

/**
 * Use JDK's HttpClient.
 * @author julius.krah
 */
@Component
public class RequestFactoryRestClientCustomizer implements RestClientCustomizer {
  @Override
  public void customize(Builder restClientBuilder) {
    restClientBuilder.requestFactory(new JdkClientHttpRequestFactory());
  }
}
