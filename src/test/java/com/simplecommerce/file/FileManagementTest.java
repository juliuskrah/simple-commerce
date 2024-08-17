package com.simplecommerce.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.ObjectStoreProperties;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author julius.krah
 */
@ExtendWith(MockitoExtension.class)
class FileManagementTest {
  @InjectMocks
  private FileManagement fileManagement;
  @Mock
  private MinioClient client;
  @Mock
  private ObjectStoreProperties properties;
  @Mock
  private Media mediaRepository;

  @Test
  void shouldResolveNode() throws MalformedURLException {
    var entity = new MediaEntity();
    entity.setId(UUID.fromString("f27d7653-dd6a-4809-8866-aef2f2e127d5"));
    entity.setContentType("image/jpeg");
    entity.setUrl(URI.create("https://play.min.io/bucket/file1.jpg").toURL());
    when(mediaRepository.findById(any())).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/MediaFile/f27d7653-dd6a-4809-8866-aef2f2e127d5
    var media = fileManagement.node("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvTWVkaWFGaWxlL2YyN2Q3NjUzLWRkNmEtNDgwOS04ODY2LWFlZjJmMmUxMjdkNQ==");
    assertThat(media).isNotNull()
        .hasFieldOrPropertyWithValue("id", "f27d7653-dd6a-4809-8866-aef2f2e127d5")
        .hasFieldOrPropertyWithValue("contentType", "image/jpeg")
        .hasFieldOrPropertyWithValue("url", URI.create("https://play.min.io/bucket/file1.jpg").toURL());
  }

  @Test
  void shouldStageUpload()
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException{
    when(properties.bucketName()).thenReturn("bucket");
    when(properties.presignedUrlExpiryDuration()).thenReturn(Duration.ofSeconds(60L));
    when(client.getPresignedObjectUrl(any())).thenReturn("http://localhost:9000/bucket/filename.jpg?X-Amz-Security-Token=token");
    var stagedUpload = fileManagement.stageUpload(new StagedUploadInput("filename.jpg", null));
    assertThat(stagedUpload).isNotNull()
        .extracting(StagedUpload::presignedUrl, StagedUpload::resourceUrl, StagedUpload::contentType)
        .containsExactly(
            URI.create("http://localhost:9000/bucket/filename.jpg?X-Amz-Security-Token=token").toURL(),
            URI.create("http://localhost:9000/bucket/filename.jpg").toURL(),
            "image/jpeg");
  }

  @Test
  void shouldStageUploadWithContentType()
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException{
    when(properties.bucketName()).thenReturn("bucket");
    when(properties.presignedUrlExpiryDuration()).thenReturn(Duration.ofSeconds(60L));
    when(client.getPresignedObjectUrl(any())).thenReturn("http://localhost:9000/bucket/file1.jpg?X-Amz-Security-Token=token");
    var stagedUpload = fileManagement.stageUpload(new StagedUploadInput("file1.jpg", "image/png"));
    assertThat(stagedUpload).isNotNull()
        .extracting(StagedUpload::contentType)
        .isEqualTo("image/png");
  }

  @Test
  void shouldAddMediaToProduct() throws MalformedURLException {
    when(mediaRepository.saveAndFlush(any())).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, MediaEntity.class);
      entity.setId(UUID.fromString("db506fa0-6c3f-4397-98c0-8828dd957dde"));
      return null;
    });
    MediaFile media = fileManagement.addMediaToProduct("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvTWVkaWFGaWxlL2YyMDk5OWU2LWUwZTUtNDdkMi04YThhLTAwMDljY2E5OGZiNQ==",
        new FileInput(URI.create("https://play.min.io/bucket/file1.jpg").toURL(), "image/jpeg"));
    assertThat(media).isNotNull()
        .hasFieldOrPropertyWithValue("id", "db506fa0-6c3f-4397-98c0-8828dd957dde")
        .hasFieldOrPropertyWithValue("contentType", "image/jpeg")
        .hasFieldOrPropertyWithValue("url", URI.create("https://play.min.io/bucket/file1.jpg").toURL());
  }
}