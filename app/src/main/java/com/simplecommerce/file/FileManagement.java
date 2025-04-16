package com.simplecommerce.file;

import static com.simplecommerce.shared.VirtualThreadHelper.callInScope;
import static com.simplecommerce.shared.VirtualThreadHelper.runInScope;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.product.ProductEntity;
import com.simplecommerce.shared.CommerceException;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import com.simplecommerce.shared.ObjectStoreProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing files and media.
 * @author julius.krah
 */
@Transactional(readOnly = true)
@Configurable(autowire = Autowire.BY_TYPE)
class FileManagement implements FileService, NodeService {
  private MinioClient client;
  private ObjectStoreProperties properties;
  private Media mediaRepository;

  public void setClient(ObjectFactory<MinioClient> client) {
    this.client = client.getObject();
  }

  public void setProperties(ObjectFactory<ObjectStoreProperties> properties) {
    this.properties = properties.getObject();
  }

  public void setMediaRepository(ObjectFactory<Media> mediaRepository) {
    this.mediaRepository = mediaRepository.getObject();
  }

  private MediaEntity toEntity(String productId, FileInput mediaFile) {
    var entity = new MediaEntity();
    var product = new ProductEntity();
    product.setId(UUID.fromString(productId));
    entity.setUrl(mediaFile.resourceUrl());
    entity.setContentType(mediaFile.contentType());
    entity.setProduct(product);
    return entity;
  }

  private MediaFile fromEntity(MediaEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new MediaFile(
        entity.getId().toString(),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getContentType(),
        entity.getUrl(),
        entity.getLastModifiedDate().orElseGet(epoch));
  }

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
    var gid = GlobalId.decode(id);
    var media = callInScope(() -> mediaRepository.findById(UUID.fromString(gid.id())));
    return media.map(this::fromEntity).orElseThrow(NotFoundException::new);
  }

  @Override
  public StagedUpload stageUpload(StagedUploadInput input) {
    var contentType = getContentType(input);
    var argsBuilder = GetPresignedObjectUrlArgs.builder()
        .method(Method.PUT)
        .bucket(properties.bucketName())
        .expiry((int) properties.presignedUrlExpiryDuration().toSeconds());
    if(contentType != null) {
      argsBuilder.extraHeaders(Map.of(HttpHeaders.CONTENT_TYPE, contentType));
    }
    try {
      var urlString = callInScope(() -> {
        var objectName = properties.mediaBucketPrefix() + "/" +
            Instant.now().toEpochMilli() +"-" + input.filename();
        argsBuilder.object(objectName);
        return client.getPresignedObjectUrl(argsBuilder.build());
      });
      URL presignedUrl = URI.create(urlString).toURL();
      URL resourceUrl = URI.create(urlString.split("\\?")[0]).toURL();
      return new StagedUpload(presignedUrl, resourceUrl, contentType);
    } catch (IOException e) {
      throw new CommerceException(e);
    }
  }

  @Override
  @Transactional
  public MediaFile addMediaToProduct(String productId, FileInput file) {
    var gid = GlobalId.decode(productId);
    var media = toEntity(gid.id(), file);
    runInScope(() -> mediaRepository.saveAndFlush(media));
    return fromEntity(media);
  }

  @Override
  public List<MediaFile> productMedia(String productId) {
    return callInScope(() -> mediaRepository.findByProductId(UUID.fromString(productId)))
        .stream().map(this::fromEntity).toList();
  }
}
