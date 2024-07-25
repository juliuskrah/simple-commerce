package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Limit;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author julius.krah
 */
@DataJpaTest(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none"
})
@Testcontainers
@RecordApplicationEvents
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductsTest {

  @Autowired
  TestEntityManager em;
  @Autowired
  private Products productRepository;

  @Container
  @ServiceConnection(type = JdbcConnectionDetails.class)
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3-alpine")
      .withMinimumRunningDuration(Duration.ofSeconds(5L));

  @Test
  void shouldSaveProduct(ApplicationEvents events) {
    var product = new ProductEntity();
    product.setTitle("DataWave");
    product.setSlug("data-wave");
    product.addTags("technology", "software", "cloud_computing");
    product.publishProductCreatedEvent();
    var entity = productRepository.saveAndFlush(product);
    assertThat(entity).isNotNull().hasNoNullFieldsOrPropertiesExcept("description")
        .extracting(ProductEntity::getTags)
        .asInstanceOf(InstanceOfAssertFactories. LIST).contains("technology", "software", "cloud_computing");
    var firedEvent = events.stream(ProductCreated.class).map(ProductCreated::product).findFirst();
    assertThat(firedEvent).isPresent()
        .get().isSameAs(product);
  }

  @Test
  void shouldFindProductById() {
    var found = productRepository.findById(UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"));
    assertThat(found).isPresent()
        .get().hasFieldOrPropertyWithValue("title", "Data Dynamo")
        .hasFieldOrPropertyWithValue("slug", "data-dynamo")
        .extracting("tags")
        .asInstanceOf(InstanceOfAssertFactories.LIST).contains("big-data", "integration", "enterprise-solutions");
  }

  @Test
  void shouldFindProductTags() {
    var tags = productRepository.findTags(
        UUID.fromString("2d02b402-570f-4c4b-932a-d5a42eae4c34"), Limit.of(5));
    assertThat(tags).isNotEmpty()
        .hasSize(5).contains("web-application", "user-experience", "ecommerce");
  }

  @Test
  void shouldFindProducts() {
    var products = productRepository.findBy(Limit.of(10));
    assertThat(products).isNotEmpty()
        .hasSize(10).extractingResultOf("getTitle")
        .contains("Data Dynamo", "Pixel Pro", "Quantum Desk");
  }

  @Test
  void shouldDeleteProduct() {
    var product = new ProductEntity();
    product.setTitle("CyberSync");
    product.setSlug("cyber-sync");

    var entity = em.persistAndFlush(product);
    // entity was successfully saved
    assertThat(entity).isNotNull();

    // delete entity (object under test)
    productRepository.deleteById(entity.getId());
    em.flush(); // flush changes to the database

    var none = em.find(ProductEntity.class, product.getId());
    assertThat(none).isNull();
  }

  @AfterAll
  static void tearDown() {
    postgres.close();
  }
}



