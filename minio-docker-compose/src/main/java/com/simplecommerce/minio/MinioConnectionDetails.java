package com.simplecommerce.minio;

import java.net.URI;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

/**
 * Connection details for MinIO.
 * @author julius.krah
 */
public interface MinioConnectionDetails extends ConnectionDetails {
  /**
   * The accessKey of the MinIO server.
   * @return the accessKey
   */
  String getAccessKey();
  /**
   * The secretKey of the MinIO server.
   * @return the secretKey
   */
  String getSecretKey();
  /**
   * The endpoint of the MinIO server.
   * @return the endpoint
   */
  URI getEndpoint();
}
