package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author julius.krah
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect")
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductsTest {
  @Autowired
  private TestEntityManager em;

  @Container
  @ServiceConnection(type = JdbcConnectionDetails.class)
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3-alpine");
//      .withMinimumRunningDuration(Duration.ofSeconds(5L));

  @Test
  void shouldSaveProduct() {
    var product = new ProductEntity();
    product.setTitle("Product");
    product.setSlug("product");
    var entity = em.persistAndFlush(product);
    assertThat(entity).isNotNull().hasNoNullFieldsOrPropertiesExcept("description");
  }

  @AfterAll
  static void tearDown() {
    postgres.close();
  }
}



