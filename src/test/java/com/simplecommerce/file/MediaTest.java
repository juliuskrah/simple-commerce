package com.simplecommerce.file;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.simplecommerce.product.ProductEntity;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MediaTest {
  @Autowired
  private Media mediaRepository;

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3-alpine")
      .withMinimumRunningDuration(Duration.ofSeconds(5L));

  @Test
  void shouldFindMediaById() throws MalformedURLException {
    var found = mediaRepository.findById(fromString("2220579f-7d74-4209-9064-2a47ab588051"));
    assertThat(found).isPresent()
        .get().hasFieldOrPropertyWithValue("contentType", "image/png")
        .hasFieldOrPropertyWithValue("url", URI.create("https://play.min.io/simple-commerce/1723896721627-big-data.png").toURL())
        .extracting("product").asInstanceOf(type(ProductEntity.class))
        .returns(fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"), from(ProductEntity::getId));
  }

  @Test
  void shouldFindMediaByProductId() throws MalformedURLException {
    var found = mediaRepository.findByProductId(fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"));
    assertThat(found).isNotEmpty()
        .hasSize(2)
        .extracting("contentType", "url").containsExactly(
            tuple("image/png", URI.create("https://play.min.io/simple-commerce/1723896721627-big-data.png").toURL()),
            tuple("image/jpeg", URI.create("https://play.min.io/simple-commerce/1723896721631-small-data.jpg").toURL()));
  }
}