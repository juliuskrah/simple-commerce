package com.simplecommerce.file;

import org.springframework.graphql.data.ArgumentValue;

record StagedUploadInput(
    String filename,
    String contentType,
    ArgumentValue<HttpMethod> httpMethod
) {
  enum HttpMethod {
    PUT,
    POST
  }
}
