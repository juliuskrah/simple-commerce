package com.simplecommerce.shared.config;

import com.simplecommerce.shared.ObjectStoreProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.credentials.StaticProvider;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ObjectStoreProperties.class)
class MinioConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(MinioConfiguration.class);
  @Value("${minio.endpoint}")
  private String endpoint;
  @Value("${minio.access-key}")
  private String accessKey;
  @Value("${minio.secret-key}")
  private String secretKey;

  @Bean
  MinioClient minioClient() {
    var provider = new StaticProvider(accessKey, secretKey, null);
    return MinioClient.builder()
      .endpoint(endpoint)
      .credentialsProvider(provider)
      .build();
  }

  @Bean
  @ConditionalOnProperty(prefix = "objectstore.options", name = "default-bucket", havingValue = "true")
  ApplicationRunner createDefaultBucket(MinioClient client, ObjectStoreProperties properties) {
    LOG.debug("Creating default bucket: {}", properties.bucketName());
    return args -> {
      var existsArgs = BucketExistsArgs.builder().bucket(properties.bucketName()).build();
      var makeArgs = MakeBucketArgs.builder().bucket(properties.bucketName()).build();
      if (!client.bucketExists(existsArgs)) {
        client.makeBucket(makeArgs);
      }
    };
  }

  @Bean
  @DependsOn("createDefaultBucket")
  @ConditionalOnResource(resources = "classpath:policies/anonymous-bucket-policy.json")
  ApplicationRunner createAnonymousBucketPolicy(MinioClient client, ObjectStoreProperties properties) {
    LOG.debug("Applying anonymous bucket policy to bucket: {}", properties.bucketName());
    return args -> {
      var existsArgs = BucketExistsArgs.builder().bucket(properties.bucketName()).build();
      if (client.bucketExists(existsArgs)) {
        var policyJson = properties.anonymousBucketPolicy().getContentAsString(StandardCharsets.UTF_8);
        var policyArgs = SetBucketPolicyArgs.builder()
          .bucket(properties.bucketName())
          .config(policyJson)
          .build();
        client.setBucketPolicy(policyArgs);
      }
    };
  }
}
