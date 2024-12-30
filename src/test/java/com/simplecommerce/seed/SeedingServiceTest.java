package com.simplecommerce.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.GlobalId;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.ExecutionGraphQlRequest;
import org.springframework.graphql.ExecutionGraphQlResponse;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import org.springframework.graphql.support.DefaultExecutionGraphQlResponse;
import reactor.core.publisher.Mono;

/**
 * @author julius.krah
 */
@ExtendWith(MockitoExtension.class)
class SeedingServiceTest {
  @InjectMocks
  private SeedingService seedingService;
  @Mock
  private ExecutionGraphQlService executionService;
  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  void shouldSeedData() throws IOException {
    stubs();
    seedingService.seed();
    var capture = ArgumentCaptor.forClass(DefaultExecutionGraphQlRequest.class);
    verification(capture);
    assertCaptured(capture.getAllValues());
  }

  private void assertCaptured(List<DefaultExecutionGraphQlRequest> request) {
    assertThat(request).isNotEmpty().hasSize(15)
        .element(0, InstanceOfAssertFactories.type(DefaultExecutionGraphQlRequest.class))
        .extracting(ExecutionGraphQlRequest::getVariables, InstanceOfAssertFactories.MAP)
        .extracting("input", InstanceOfAssertFactories.MAP)
        .containsOnlyKeys("description", "tags", "title")
        .containsExactly(
            entry("description", ""),
            entry("title", "Doom: Part One"),
            entry("tags", List.of("movie")));
    // Assert stage product media
    assertThat(request).element(5, InstanceOfAssertFactories.type(DefaultExecutionGraphQlRequest.class))
        .extracting(ExecutionGraphQlRequest::getVariables, InstanceOfAssertFactories.MAP)
        .extracting("input", InstanceOfAssertFactories.MAP)
        .containsOnlyKeys("filename");
  }

  private void verification(ArgumentCaptor<DefaultExecutionGraphQlRequest> captor) throws IOException {
    // 1. Called 5 times insert products
    // 2. Called 10 times stage product media
    verify(executionService, times(15)).execute(captor.capture());
    verify(objectMapper, times(2)).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Set<Map<String, Object>>>>any());
    verify(resourceLoader, times(1)).getResource(argThat(location -> location.contains("products.json")));
    verify(resourceLoader, times(1)).getResource(argThat(location -> location.contains("product_media.json")));
    verify(resourceLoader, times(5)).getResource(argThat(location -> location.contains("media/doom-part-one")));
    verify(resourceLoader, times(5)).getResource(argThat(location -> location.contains("media/doom-part-two")));
  }

  @SuppressWarnings("unchecked")
  private void stubs() throws IOException {
    var resource = spy(new ClassPathResource("seed-data/media/test-image.jpeg"));
    when(executionService.execute(any(ExecutionGraphQlRequest.class))).thenAnswer(answer(this::answerRequest));
    when(resourceLoader.getResource(anyString())).thenReturn(resource);
    when(objectMapper.readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Set<Map<String, Object>>>>any()))
        .thenReturn(products(), productMedia());
  }

  @SuppressWarnings("unchecked")
  private boolean matchProductRequest(ExecutionGraphQlRequest request) {
    var input = (Map<String, String>) request.getVariables().get("input");
    return input.containsKey("tags")
        && input.containsKey("title")
        && input.containsKey("description");
  }

  @SuppressWarnings("unchecked")
  private boolean matchStagedUploadRequest(ExecutionGraphQlRequest request) {
    var input = (Map<String, String>) request.getVariables().get("input");
    return input.containsKey("filename");
  }

  private Mono<ExecutionGraphQlResponse> createCreateProductResponse() {
    var input = ExecutionInput.newExecutionInput().query("mutation {}").build();
    String id = new GlobalId("Product", UUID.randomUUID().toString()).encode();
    var output = ExecutionResult.newExecutionResult().data(Map.of(
        "addProduct", Map.of(
            "id", id))).build();
    ExecutionGraphQlResponse response = new DefaultExecutionGraphQlResponse(input, output);
    return Mono.defer(() -> Mono.just(response));
  }

  private Mono<ExecutionGraphQlResponse> stagedUploadResponse() {
    var input = ExecutionInput.newExecutionInput().query("mutation {}").build();
    Random random = new Random();
    var randomString = randomString(random);
    var output = ExecutionResult.newExecutionResult().data(Map.of(
        "stagedUpload", Map.of(
            "presignedUrl", "https://play.minio.io/bucket/prefix/object.jpg?signature=signature&expires=expires",
            "resourceUrl", "https://play.minio.io/bucket/prefix/%s.jpg".formatted(randomString),
            "contentType", "image/jpeg")))
        .build();
    ExecutionGraphQlResponse response = new DefaultExecutionGraphQlResponse(input, output);
    return Mono.defer(() -> Mono.just(response));
  }

  private String randomString(Random random) {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    long targetStringLength = 10;
    return random.ints(leftLimit, rightLimit + 1)
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  private Mono<ExecutionGraphQlResponse> answerRequest(ExecutionGraphQlRequest request) {
    if (matchStagedUploadRequest(request)) {
      return stagedUploadResponse();
    } else if(matchProductRequest(request)) {
      return createCreateProductResponse();
    }
    return null;
  }

  private Set<Map<String, Object>> products() {
    var products = new HashSet<Map<String, Object>>();
    products.add(new HashMap<>(Map.of(
        "id", "4e8343d7-8dbe-408f-bd82-3732d6c3793d",
        "title", "Doom: Part One",
        "description",  "",
        "tags", List.of("movie"))));
    products.add(new HashMap<>(Map.of(
        "id", "512af1f2-acc0-4464-a6fb-71236b12ff43",
        "title", "Doom: Part Two",
        "description", "",
        "tags", List.of("movie"))));
    products.add(new HashMap<>(Map.of(
        "id", "f88769c0-ee7c-4b9a-aa6e-63b45657328b",
        "title", "Hannah Putin and the Philosopher's Stone",
        "description", "",
        "tags", List.of("movie"))));
    products.add(new HashMap<>(Map.of(
        "id", "04906a83-7e6e-4066-92f4-1caff5857fec",
        "title", "Hannah Putin and the Chamber of Secrets",
        "description", "",
        "tags", List.of("movie"))));
    products.add(new HashMap<>(Map.of(
        "id", "f0c8198b-59d1-4a5a-a577-e9aee3d01946",
        "title", "Hannah Putin and the Prisoner of Azkaban",
        "description", "",
        "tags", List.of("movie"))));
    return products;
  }

  private Set<Map<String, Object>> productMedia() {
    var productMedia = new HashSet<Map<String, Object>>();
    productMedia.add(new HashMap<>(Map.of(
        "productId", "4e8343d7-8dbe-408f-bd82-3732d6c3793d",
        "mediaLocations", List.of(
            "media/doom-part-one-1.jpg",
            "media/doom-part-one-2.jpg",
            "media/doom-part-one-3.jpg",
            "media/doom-part-one-4.jpg",
            "media/doom-part-one-5.jpg")
    )));
    productMedia.add(new HashMap<>(Map.of(
        "productId", "512af1f2-acc0-4464-a6fb-71236b12ff43",
        "mediaLocations", List.of(
            "media/doom-part-two-1.jpg",
            "media/doom-part-two-2.jpg",
            "media/doom-part-two-3.jpg",
            "media/doom-part-two-4.jpg",
            "media/doom-part-two-5.jpg")
    )));
    return productMedia;
  }
}