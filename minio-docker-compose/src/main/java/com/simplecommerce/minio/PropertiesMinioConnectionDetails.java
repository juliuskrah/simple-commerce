package com.simplecommerce.minio;

import java.net.URI;

/**
 * PropertiesMinioConnectionDetails is a class that implements the MinioConnectionDetails interface. It provides the connection details for MinIO using properties.
 *
 * @author julius.krah
 */
record PropertiesMinioConnectionDetails(MinioProperties minioProperties) implements MinioConnectionDetails {

  @Override
  public String getAccessKey() {
    return minioProperties.accessKey();
  }

  @Override
  public String getSecretKey() {
    return minioProperties.secretKey();
  }

  @Override
  public URI getEndpoint() {
    return minioProperties.endpoint();
  }
}
