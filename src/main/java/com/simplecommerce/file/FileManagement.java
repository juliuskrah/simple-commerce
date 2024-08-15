package com.simplecommerce.file;

import com.simplecommerce.file.StagedUploadInput.HttpMethod;
import com.simplecommerce.node.NodeService;
import com.simplecommerce.shared.CommerceException;
import com.simplecommerce.shared.ObjectStoreProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpHeaders;

/**
 * @author julius.krah
 */
@Configurable(autowire = Autowire.BY_TYPE)
class FileManagement implements FileService, NodeService {
  @Autowired
  private MinioClient client;
  @Autowired
  private ObjectStoreProperties properties;

  private String getContentType(StagedUploadInput input) {
    if(input.contentType() != null) {
      return input.contentType();
    }
    var path = Paths.get(input.filename());
    try {
      return Files.probeContentType(path);
    } catch (IOException e) {
      // ignore
      return null;
    }
  }

  @Override
  public File node(String id) {
    // TODO Write implementation
    return null;
  }

  @Override
  public StagedUpload stageUpload(StagedUploadInput input) {
    var contentType = getContentType(input);
    var argsBuilder = GetPresignedObjectUrlArgs.builder()
        .method(Method.valueOf(input.httpMethod().asOptional().orElse(HttpMethod.PUT).name()))
        .bucket(properties.bucketName())
        .object(input.filename())
        .expiry((int) properties.presignedUrlExpiryDuration().toSeconds());
    if(contentType != null) {
      argsBuilder.extraHeaders(Map.of(HttpHeaders.CONTENT_TYPE, contentType));
    }
    URL presignedUrl;
    URL resourceUrl;
    try {
      var urlString = client.getPresignedObjectUrl(argsBuilder.build());
      presignedUrl =  URI.create(urlString).toURL();
      resourceUrl = URI.create(urlString.split("\\?")[0]).toURL();
    } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
             ServerException e) {
      throw new CommerceException(e);
    }
    return new StagedUpload(presignedUrl, resourceUrl);
  }
}
