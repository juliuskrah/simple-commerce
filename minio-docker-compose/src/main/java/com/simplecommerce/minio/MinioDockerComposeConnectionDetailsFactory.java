package com.simplecommerce.minio;

import java.net.URI;
import java.util.Map;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * MinioDockerComposeConnectionDetailsFactory is a factory class that creates MinioConnectionDetails instances for Docker Compose.
 * It extends the DockerComposeConnectionDetailsFactory class and provides the implementation for creating MinioConnectionDetails.
 *
 * @author julius.krah
 */
class MinioDockerComposeConnectionDetailsFactory extends DockerComposeConnectionDetailsFactory<MinioConnectionDetails> {

  private static final String[] MINIO_CONTAINER_NAMES = { "minio", "minio/minio" };

  protected MinioDockerComposeConnectionDetailsFactory() {
    super(MINIO_CONTAINER_NAMES);
  }

  @Override
  protected MinioConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
    return new MinioDockerComposeConnectionDetails(source.getRunningService());
  }

  /**
   * {@link MinioConnectionDetails} backed by a {@code minio} {@link RunningService}.
   */
  static class MinioDockerComposeConnectionDetails extends DockerComposeConnectionDetails
      implements MinioConnectionDetails {

    private static final String SCHEME_LABEL = "com.simplecommerce.endpoint.scheme";
    final Map<String, String> env;
    final RunningService service;

    /**
     * Create a new {@link DockerComposeConnectionDetails} instance.
     *
     * @param runningService the source {@link RunningService}
     */
    protected MinioDockerComposeConnectionDetails(RunningService runningService) {
      super(runningService);
      this.env = runningService.env();
      this.service = runningService;
    }

    @Override
    public String getAccessKey() {
      var accessKey = env.get("MINIO_ROOT_USER");
      Assert.state(StringUtils.hasLength(accessKey), "MINIO_ROOT_USER environment variable not set");
      return accessKey;
    }

    @Override
    public String getSecretKey() {
      var secretKey = env.get("MINIO_ROOT_PASSWORD");
      Assert.state(StringUtils.hasLength(secretKey), "MINIO_ROOT_PASSWORD environment variable not set");
      return secretKey;
    }

    @Override
    public URI getEndpoint() {
      var endpoint = buildEndpoint();
      return URI.create(endpoint);
    }

    private String buildEndpoint() {
      return "%s://%s:%s".formatted(
          service.labels().get(SCHEME_LABEL),
          service.host(), service.ports().get(9000));
    }
  }
}
