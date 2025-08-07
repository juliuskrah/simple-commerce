package com.simplecommerce.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.execution.ExecutionId;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author julius.krah
 */
@Service
@ConditionalOnProperty(prefix = "simple-commerce.seeder", name = "enabled", havingValue = "true")
public class SeedingService {

  private static final Logger LOG = LoggerFactory.getLogger(SeedingService.class);
  private final ExecutionGraphQlService executionService;
  private final ResourceLoader resourceLoader;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;
  @Value("${simple-commerce.seeder.directory-prefix}")
  private String directoryPrefix;

  public SeedingService(ExecutionGraphQlService executionService, ResourceLoader resourceLoader, ObjectMapper objectMapper, RestClient restClient) {
    this.executionService = executionService;
    this.resourceLoader = resourceLoader;
    this.objectMapper = objectMapper;
    this.restClient = restClient;
  }

  private void seedProductMedia(Flux<Product> products) throws IOException {
    var addProductToMedia = """
        mutation addMediaToProduct($id: ID!, $file: FileInput!) {
          addProductMedia(productId: $id, file: $file) {
            id
            url
          }
        }
        """;
    var productMedia = loadResource(directoryPrefix + "product_media.json");
    var productMediaVariables = objectMapper.readValue(productMedia.getInputStream(),
        new TypeReference<Set<Map<String, Object>>>() {
        });
    Flux<Map<String, Object>> mediaVariables = products.map(product ->
      productMediaVariables.stream()
          .filter(media -> media.get("productId") == product.syntheticId())
          .map(media -> {media.put("id", product.naturalId()); return media;})
          .collect(Collectors.toSet())
    ).flatMap(Flux::fromIterable);
    uploadMedia(mediaVariables).blockLast();
  }

  private Flux<Map<String, Object>> uploadMedia(Flux<Map<String, Object>> mediaVariables) {
    return Flux.from(mediaVariables)
        .flatMap(variable -> {
          @SuppressWarnings("unchecked")
          var images = (List<String>) variable.get("mediaLocations");
          return Flux.fromIterable(images)
              .flatMap(this::createStaged)
              .publishOn(Schedulers.boundedElastic())
              .map(staged -> uploadMedia(staged, variable));
        }).log();
  }

  private Flux<Product> seedProduct() throws IOException {
    var makeProduct = """
        mutation makeProduct($input: ProductInput!) {
          addProduct(input: $input) {
            id
          }
        }
        """;

    var products = loadResource(directoryPrefix + "products.json");
    var productVariables = objectMapper.readValue(products.getInputStream(),
        new TypeReference<Set<Map<String, Object>>>(){});

    return Flux.fromIterable(productVariables)
        .flatMap(variable -> {
          var product = new Product((String)variable.get("id"), null);
          removeId(variable, Set.of("id"));
          var result = executionService.execute(new DefaultExecutionGraphQlRequest(
                makeProduct, null, Map.of("input", variable), null, id(), null));
          return result.map(response -> {
            String nodeId = response.field("addProduct.id").getValue();
            return product.withNaturalId(nodeId);
          });
        })
        .subscribeOn(Schedulers.boundedElastic())
        .log();
  }

  private void removeId(Map<String, Object> products, Set<String> keysToRemove) {
    products.keySet().removeAll(keysToRemove);
  }

  private String id() {
    return ExecutionId.generate().toString();
  }

  private Mono<Map<String, Object>> createStaged(String filePath) {
    var stageUpload = """
        mutation stageUpload($input: StagedUploadInput!) {
          stagedUpload(input: $input) {
            presignedUrl
            resourceUrl
            contentType
          }
        }
        """;
    var fileName = Paths.get(filePath).getFileName().toString();
    Map<String, Object> input = Map.of("input", Map.of("filename", fileName));
    var stagedUpload = new DefaultExecutionGraphQlRequest(
        stageUpload, null, input, null, id(), null);
    return executionService.execute(stagedUpload).map(response -> {
      response.field("addProduct.id").getValue();
      String presignedUrl = response.field("stagedUpload.presignedUrl").getValue();
      String resourceUrl = response.field("stagedUpload.resourceUrl").getValue();
      String contentType = response.field("stagedUpload.contentType").getValue();
      return Map.of("presignedUrl", presignedUrl, "resourceUrl", resourceUrl,
          "contentType", contentType, "mediaPath", filePath);
    });
  }

  private Map<String, Object> uploadMedia(
      Map<String, Object> stagedMedia, Map<String, Object> variable) {
    var mediaPath = (String) stagedMedia.get("mediaPath");
    var contentType = (String) stagedMedia.get("contentType");
    var resource = resourceLoader.getResource(directoryPrefix + mediaPath);
    try {
      var response = restClient.put().uri(URI.create((String) stagedMedia.get("presignedUrl")))
          .header(HttpHeaders.CONTENT_TYPE, contentType)
          .body(resource)
          .retrieve().toEntity(String.class);
      LOG.info("Upload status: {}", response.getStatusCode());
    } catch (Exception e) {
      LOG.info("Failed to upload media [{}] to {}", mediaPath, stagedMedia.get("presignedUrl"), e);
    }
    return Map.of(
        "file", Map.of(
            "contentType", contentType,
            "resourceUrl", stagedMedia.get("resourceUrl")),
        "id", variable.get("id"));
  }

  private Resource loadResource(String path) {
    return resourceLoader.getResource(path);
  }

  public void seed() throws IOException {
    // TODO drop existing data
    var products = seedProduct();
    seedProductMedia(products);
  }

  /**
   * Seed the database when the application is ready.
   * @param event ApplicationReadyEvent
   */
  @Async
  @EventListener
  void on(ApplicationReadyEvent event) {
    try {
      LOG.info("Starting database seeding process...");
      seed();
      LOG.info("Database seeding completed successfully");
    } catch (IOException e) {
      LOG.error("Failed to seed database", e);
      throw new RuntimeException("Database seeding failed", e);
    }
  }

  record Product(String syntheticId, String naturalId) {
    Product withNaturalId(String naturalId) {
      return new Product(syntheticId, naturalId);
    }
  }
}
