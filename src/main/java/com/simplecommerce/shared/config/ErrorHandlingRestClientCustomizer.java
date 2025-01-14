package com.simplecommerce.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.Builder;

/**
 * @author julius.krah
 */
@Component
public class ErrorHandlingRestClientCustomizer implements RestClientCustomizer {
  private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlingRestClientCustomizer.class);

  @Override
  public void customize(Builder restClientBuilder) {
    restClientBuilder.defaultStatusHandler(HttpStatusCode::isError, (request, response) ->
        LOG.warn("Current request: {} failed with status: {}", response.getStatusCode(), request.getURI()));
  }
}
