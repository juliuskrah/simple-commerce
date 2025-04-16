package com.simplecommerce.minio;

import io.minio.MinioClient;
import io.minio.credentials.StaticProvider;
import java.net.MalformedURLException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for MinIO.
 * @author julius.krah
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.minio.MinioClient")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

  /**
   * Creates a {@link MinioClient} bean.
   * @return a {@link MinioClient} bean
   */
  @Bean
  @ConditionalOnMissingBean(MinioClient.class)
  MinioClient minioClient(ObjectProvider<MinioConnectionDetails> connectionDetailsProvider) throws MalformedURLException {
    var connectionDetails = connectionDetailsProvider.getObject();
    var provider = new StaticProvider(
        connectionDetails.getAccessKey(),
        connectionDetails.getSecretKey(),
        null
    );
    return MinioClient.builder()
        .endpoint(connectionDetails.getEndpoint().toURL())
        .credentialsProvider(provider)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(MinioConnectionDetails.class)
  PropertiesMinioConnectionDetails minioConnectionDetails(MinioProperties properties) {
    return new PropertiesMinioConnectionDetails(properties);
  }
}
