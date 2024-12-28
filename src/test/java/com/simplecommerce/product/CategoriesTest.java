package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataPostgresTest
class CategoriesTest {
  @Autowired TestEntityManager em;
  @Autowired Categories categoryRepository;

  @Test
  void shouldFindProductById() {
    var found = categoryRepository.findById(UUID.fromString("a0dab2f2-fb3d-40ed-bacf-f31dedd10f45"));
    assertThat(found).isPresent()
        .get().hasFieldOrPropertyWithValue("title", "Bundles")
        .hasFieldOrPropertyWithValue("slug", "bundles")
        .hasFieldOrPropertyWithValue("path", "Bundles");
  }
}