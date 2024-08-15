package com.simplecommerce.shared.config;

import com.simplecommerce.shared.ObjectStoreProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ObjectStoreProperties.class)
class MinioConfiguration {
  private static final Logger log = LoggerFactory.getLogger(MinioConfiguration.class);
  @Value("${minio.endpoint}")
  private String endpoint;
  @Value("${minio.access-key}")
  private String accessKey;
  @Value("${minio.secret-key}")
  private String secretKey;

  @Bean
  MinioClient minioClient() {
    return MinioClient.builder()
      .endpoint(endpoint)
      .credentials(accessKey, secretKey)
      .build();
  }

  @Bean
  @ConditionalOnProperty(prefix = "objectstore.options", name = "default-bucket", havingValue = "true")
  ApplicationRunner createDefaultBucket(MinioClient client, ObjectStoreProperties properties) {
    log.debug("Creating default bucket: {}", properties.bucketName());
    return args -> {
      var existsArgs = BucketExistsArgs.builder().bucket(properties.bucketName()).build();
      var makeArgs = MakeBucketArgs.builder().bucket(properties.bucketName()).build();
      if (!client.bucketExists(existsArgs)) {
        client.makeBucket(makeArgs);
      }
    };
  }
}
