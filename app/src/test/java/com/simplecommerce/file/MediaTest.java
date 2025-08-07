package com.simplecommerce.file;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.simplecommerce.DataPostgresTest;
import com.simplecommerce.product.ProductEntity;
import java.net.MalformedURLException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
class MediaTest {
  @Autowired
  private Media mediaRepository;

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