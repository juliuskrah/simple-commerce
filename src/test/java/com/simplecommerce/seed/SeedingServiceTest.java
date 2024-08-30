package com.simplecommerce.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Set;
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

@ExtendWith(MockitoExtension.class)
class SeedingServiceTest {
  @InjectMocks
  private SeedingService seedingService;
  @Mock
  private ExecutionGraphQlService executionService;
  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  ObjectMapper objectMapper;

  @Test
  void shouldSeedData() throws IOException {
    stubs();
    seedingService.seed();
    var capture = ArgumentCaptor.forClass(DefaultExecutionGraphQlRequest.class);
    verification(capture);
    assertCaptured(capture.getAllValues());
  }

  private void assertCaptured(List<DefaultExecutionGraphQlRequest> request) {
    assertThat(request).isNotEmpty().hasSize(5)
        .element(0, InstanceOfAssertFactories.type(DefaultExecutionGraphQlRequest.class))
        .extracting(ExecutionGraphQlRequest::getVariables, InstanceOfAssertFactories.MAP)
        .containsOnlyKeys("description", "tags", "title")
        .containsExactly(
            entry("description", ""),
            entry("title", "Doom: Part One"),
            entry("tags", List.of("movie")));
  }

  private void verification(ArgumentCaptor<DefaultExecutionGraphQlRequest> captor) throws IOException {
    verify(executionService, times(5)).execute(captor.capture());
    verify(objectMapper, times(1)).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Set<Map<String, Object>>>>any());
    verify(resourceLoader, times(1)).getResource(anyString());
  }

  private void stubs() throws IOException {
    var resource = spy(new ClassPathResource(""));
    when(executionService.execute(argThat(this::matchProduct))).thenReturn(createResponse());
    when(resourceLoader.getResource(anyString())).thenReturn(resource);
    when(objectMapper.readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Set<Map<String, Object>>>>any()))
        .thenReturn(products());
  }

  private boolean matchProduct(ExecutionGraphQlRequest request) {
    return request.getVariables().containsKey("tags")
        && request.getVariables().containsKey("title")
        && request.getVariables().containsKey("description");
  }

  private Mono<ExecutionGraphQlResponse> createResponse() {
    var input = ExecutionInput.newExecutionInput().query("mutation {}").build();
    var output = ExecutionResult.newExecutionResult().data(Map.of(
        "addProduct", Map.of(
            "id", "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC9lNWVhODE5YS05NjQ1LTRiMTQtOWVjMy00NzgzZGY0Mzc0OWI="))).build();
    ExecutionGraphQlResponse response = new DefaultExecutionGraphQlResponse(input, output);
    return Mono.defer(() -> Mono.just(response));
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
}