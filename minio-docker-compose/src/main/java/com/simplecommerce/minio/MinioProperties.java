package com.simplecommerce.minio;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for MinIO.
 * @author julius.krah
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
    URI endpoint,
    String accessKey,
    String secretKey
) {

}
