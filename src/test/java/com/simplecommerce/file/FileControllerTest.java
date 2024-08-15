package com.simplecommerce.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;

/**
 * @author julius.krah
 */
@GraphQlTest(FileController.class)
class FileControllerTest {
  @MockBean
  private FileService fileService;

  @Autowired
  private ExecutionGraphQlServiceTester graphQlTester;

  @Test
  void shouldStageUpload() throws MalformedURLException {
    var stagedUpload = new StagedUpload(
        URI.create("https://play.min.io:9443/simple-commerce/example.png").toURL(),
        URI.create("https://play.min.io:9443/simple-commerce/example.png").toURL());
    when(fileService.stageUpload(any(StagedUploadInput.class))).thenReturn(stagedUpload);
    graphQlTester.documentName("stagedUpload")
        .variable("input", Map.of("filename", "some-file.jpg"))
        .execute()
        .path("stagedUpload").entity(StagedUpload.class)
        .matches(upload ->
            "play.min.io".equals(upload.presignedUrl().getHost()))
        .satisfies(upload -> assertThat(upload.resourceUrl()).isNotNull());
  }
}
