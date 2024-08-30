package com.simplecommerce.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.execution.ExecutionId;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class SeedingService {

  private final ExecutionGraphQlService executionService;
  private final ResourceLoader resourceLoader;
  private final ObjectMapper objectMapper;
  @Value("${simple-commerce.seeder.directory-prefix}")
  private String directoryPrefix;

  public SeedingService(ExecutionGraphQlService executionService, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
    this.executionService = executionService;
    this.resourceLoader = resourceLoader;
    this.objectMapper = objectMapper;
  }

  private void seedProductMedia(Set<Product> products) throws IOException {
    var stageUpload = """
        mutation stageUpload($input: StagedUploadInput!) {
          stagedUpload(input: $input) {
            presignedUrl
            resourceUrl
            contentType
          }
        }
        """;
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
    products.forEach(product ->
      productMediaVariables.stream()
          .filter(media -> media.get("productId") == product.syntheticId())
          .forEach(media -> media.put("id", product.naturalId()))
    );
    Flux.fromIterable(productMediaVariables)
        .flatMap(variable -> {
          @SuppressWarnings("unchecked")
          var images = (List<Map<String, Object>>) variable.get("mediaLocations");
          return Flux.fromIterable(images);
        });
  }

  private Set<Product> seedProduct() throws IOException {
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
          var requestVariables = removeId(variable);
          var result = executionService.execute(new DefaultExecutionGraphQlRequest(
                makeProduct, null, requestVariables, null, id(), null));
          return result.map(response -> {
            String nodeId = response.field("addProduct.id").getValue();
            return product.withNaturalId(nodeId);
          });
        })
        .subscribeOn(Schedulers.boundedElastic())
        .log()
        .collect(Collectors.toSet())
        .block();
  }

  private Map<String, Object> removeId(Map<String, Object> products) {
    var keysToRemove = Set.of("id");
    products.keySet().removeAll(keysToRemove);
    return products;
  }

  private String id() {
    return ExecutionId.generate().toString();
  }

  private Resource loadResource(String path) {
    return resourceLoader.getResource(path);
  }

  public void seed() throws IOException {
    var products = seedProduct();
//    seedProductMedia(products);
  }

  record Product(String syntheticId, String naturalId) {
    Product withNaturalId(String naturalId) {
      return new Product(syntheticId, naturalId);
    }
  }
}
