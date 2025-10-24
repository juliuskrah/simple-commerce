package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.tuple;

import com.simplecommerce.DataPostgresTest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ProductsTest {
  @Autowired
  TestEntityManager em;
  @Autowired
  private Products productRepository;

  @Test
  void shouldSaveProduct(ApplicationEvents events) {
    var product = new ProductEntity();
    product.setTitle("DataWave");
    product.setSlug("data-wave");
    product.addTags("technology", "software", "cloud_computing");
    var entity = productRepository.saveAndFlush(product);
    assertThat(entity).isNotNull()
        .hasNoNullFieldsOrPropertiesExcept("description", "createdBy", "updatedBy", "category")
        .extracting(ProductEntity::getTags)
        .asInstanceOf(InstanceOfAssertFactories. LIST).contains("technology", "software", "cloud_computing");
    var firedEvent = events.stream(ProductEvent.class).map(ProductEvent::source).findFirst();
    assertThat(firedEvent).isPresent()
        .get().isSameAs(product);
  }

  @Test
  void shouldFindProductById() {
    var found = productRepository.findById(UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"));
    assertThat(found).isPresent()
        .get().hasFieldOrPropertyWithValue("title", "Data Dynamo")
        .hasFieldOrPropertyWithValue("slug", "data-dynamo")
        .extracting(ProductEntity::getTags, product -> product.getCategory().getTitle())
        .contains("Uncategorized", atIndex(1)) // Default category
        .element(0, as(InstanceOfAssertFactories.LIST))
        .contains("big-data", "integration", "enterprise-solutions");
  }

  @Test
  void shouldFindProductTags() {
    var tags = productRepository.findTags(
        UUID.fromString("2d02b402-570f-4c4b-932a-d5a42eae4c34"), Limit.of(5));
    assertThat(tags).isNotEmpty()
        .hasSize(5).contains("web-application", "user-experience", "ecommerce");
  }

  @Test
  void shouldFindProductWithTags() {
    var tags = productRepository.findTags(2, Set.of(
        UUID.fromString("2d02b402-570f-4c4b-932a-d5a42eae4c34"),
        UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858")));
    assertThat(tags).isNotEmpty().hasSize(2)
        .extracting("id", "tags")
        .contains(
            tuple(UUID.fromString("2d02b402-570f-4c4b-932a-d5a42eae4c34"),
                List.of("productivity", "digital-transformation")),
            tuple(UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"),
                List.of("integration", "enterprise-solutions")));
  }

  @Test
  void shouldFindProducts() {
    var products = productRepository.findBy(Limit.of(10));
    assertThat(products).isNotEmpty()
        .hasSize(10).extractingResultOf("getTitle")
        .contains("Data Dynamo", "Pixel Pro", "Virtual Vault");
  }

  @Test
  void shouldFindProductsByScrolling() {
    var window = productRepository.findBy(Limit.of(10), Sort.unsorted(), ScrollPosition.keyset());
    assertThat(window).isNotEmpty()
        .hasSize(10).extractingResultOf("getTitle")
        .contains(
            "Sync Fusion",
            "Pixel Forge",
            "Alpha Stream",
            "Pixel Pro",
            "Nex Tech",
            "Data Dynamo",
            "Code Pulse",
            "Logic Flow",
            "Tech Verse",
            "Virtual Vault");
  }

  @Test
  void shouldFindProductsByCategoryId() {
    var id = UUID.fromString("6ef9c5ce-0430-468e-8adb-523fc05c4a11"); // Uncategorized - Default category
    var products = productRepository.findByCategoryId(id, Limit.unlimited(), Sort.unsorted(), ScrollPosition.keyset());
    assertThat(products).isNotEmpty()
        .hasSize(20).extractingResultOf("getTitle")
        .contains(
            "Sync Fusion",
            "Pixel Forge",
            "Alpha Stream",
            "Pixel Pro",
            "Nex Tech",
            "Data Dynamo",
            "Code Pulse",
            "Logic Flow",
            "Tech Verse",
            "Virtual Vault");
  }

  @Test
  void shouldFindProductsByScrollingAfterKeys() {
    var window = productRepository.findBy(Limit.of(10), Sort.unsorted(), ScrollPosition.forward(Map.of(
        "title", "Virtual Vault", "id", UUID.fromString("8a293c02-33f9-4bdb-96b9-3e7d4f753666"))
    ));
    assertThat(window).isNotEmpty()
        .hasSize(10).extractingResultOf("getTitle")
        .contains(
            "Virtu Sync",
            "Data Haven",
            "Code Craft",
            "Super Vault",
            "Vertex Cloud",
            "Data Pro",
            "Cyber Sphere",
            "Quantum Desk",
            "Byte Wave",
            "Opti Core");
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

}



