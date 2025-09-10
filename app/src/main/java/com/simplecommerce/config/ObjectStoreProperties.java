package com.simplecommerce.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * @author julius.krah
 */
@ConfigurationProperties(prefix = "objectstore.options")
public record ObjectStoreProperties(
    Resource anonymousBucketPolicy,
    String bucketName,
    String mediaBucketPrefix,
    Duration presignedUrlExpiryDuration
) { }
