package com.simplecommerce.shared;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author julius.krah
 */
@ConfigurationProperties(prefix = "objectstore.options")
public record ObjectStoreProperties(
    String bucketName,
    Duration presignedUrlExpiryDuration
) { }
