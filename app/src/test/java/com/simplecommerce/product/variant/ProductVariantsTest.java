package com.simplecommerce.product.variant;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import com.simplecommerce.product.ProductEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for ProductVariants repository.
 * @author julius.krah
 */
@DataPostgresTest
@ActiveProfiles("test")
class ProductVariantsTest {

  @Autowired
  TestEntityManager em;
  @Autowired
  private ProductVariants variantRepository;

  @Test
  void shouldFindVariantById() {
    var variantId = UUID.fromString("a1b2c3d4-1111-2222-3333-444455556666");
    var found = variantRepository.findById(variantId);

    assertThat(found).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("sku", "DD-DEFAULT")
        .hasFieldOrPropertyWithValue("title", "Default")
        .hasFieldOrPropertyWithValue("priceAmount", new BigDecimal("99.9900"))
        .hasFieldOrPropertyWithValue("priceCurrency", "USD")
        .hasFieldOrPropertyWithValue("systemGenerated", true);
  }

  @Test
  void shouldReturnEmptyWhenVariantNotFound() {
    var variantId = UUID.randomUUID();
    var found = variantRepository.findById(variantId);

    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindVariantsByProductId() {
    var productId = UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"); // Data Dynamo
    var window = variantRepository.findByProductId(
        productId,
        Limit.of(10),
        Sort.by("title"),
        ScrollPosition.keyset());

    assertThat(window).isNotEmpty()
        .hasSize(4)
        .extracting("sku")
        .containsExactly("DD-DEFAULT", "DD-LARGE", "DD-MEDIUM", "DD-SMALL");
  }

  @Test
  void shouldFindVariantsByProductIdWithScrolling() {
    var productId = UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858");
    var window = variantRepository.findByProductId(
        productId,
        Limit.of(2),
        Sort.by("title"),
        ScrollPosition.keyset());

    assertThat(window).isNotEmpty()
        .hasSize(2)
        .extracting("sku")
        .containsExactly("DD-DEFAULT", "DD-LARGE");

    // Test that we can scroll to the next page
    assertThat(window.hasNext()).isTrue();
  }

  @Test
  void shouldReturnEmptyWindowWhenNoVariantsForProduct() {
    var productId = UUID.fromString("2b45edab-9d8c-4f5b-9876-8a8ae2e0a372"); // Alpha Stream (no variants in test data)
    var window = variantRepository.findByProductId(
        productId,
        Limit.of(10),
        Sort.unsorted(),
        ScrollPosition.keyset());

    assertThat(window).isEmpty();
  }

  @Test
  void shouldCountVariantsByProductId() {
    var productId = UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"); // Data Dynamo with 4 variants
    var count = variantRepository.countByProductId(productId);

    assertThat(count).isEqualTo(4L);
  }

  @Test
  void shouldReturnZeroCountWhenNoVariantsForProduct() {
    var productId = UUID.fromString("2b45edab-9d8c-4f5b-9876-8a8ae2e0a372"); // Alpha Stream (no variants)
    var count = variantRepository.countByProductId(productId);

    assertThat(count).isZero();
  }

  @Test
  void shouldFindVariantBySku() {
    var found = variantRepository.findBySku("DD-MEDIUM");

    assertThat(found).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("title", "Medium")
        .hasFieldOrPropertyWithValue("priceAmount", new BigDecimal("99.9900"))
        .hasFieldOrPropertyWithValue("systemGenerated", false);
  }

  @Test
  void shouldReturnEmptyWhenSkuNotFound() {
    var found = variantRepository.findBySku("NON-EXISTENT-SKU");

    assertThat(found).isEmpty();
  }

  @Test
  void shouldSaveAndFlushVariant() {
    var product = em.find(ProductEntity.class, UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"));

    var variant = new ProductVariantEntity();
    variant.setProduct(product);
    variant.setSku("DD-XL");
    variant.setTitle("Extra Large");
    variant.setPriceAmount(new BigDecimal("139.99"));
    variant.setPriceCurrency("USD");
    variant.setSystemGenerated(false);

    var saved = variantRepository.saveAndFlush(variant);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();

    // Verify it was actually persisted
    var found = em.find(ProductVariantEntity.class, saved.getId());
    assertThat(found)
        .isNotNull()
        .hasFieldOrPropertyWithValue("sku", "DD-XL")
        .hasFieldOrPropertyWithValue("title", "Extra Large");
  }

  @Test
  void shouldUpdateExistingVariant() {
    var variantId = UUID.fromString("a1b2c3d4-2222-3333-4444-555566667777");
    var variant = em.find(ProductVariantEntity.class, variantId);

    variant.setPriceAmount(new BigDecimal("84.99"));
    variant.setTitle("Small (Updated)");

    variantRepository.saveAndFlush(variant);
    em.clear(); // Clear persistence context to force a database read

    var updated = em.find(ProductVariantEntity.class, variantId);
    assertThat(updated)
        .hasFieldOrPropertyWithValue("priceAmount", new BigDecimal("84.9900"))
        .hasFieldOrPropertyWithValue("title", "Small (Updated)");
  }

  @Test
  void shouldDeleteVariantById() {
    var product = em.find(ProductEntity.class, UUID.fromString("062a179c-ea99-43ab-9d31-4f0968de49f9"));

    var variant = new ProductVariantEntity();
    variant.setProduct(product);
    variant.setSku("SF-TEMP");
    variant.setTitle("Temporary");
    variant.setPriceAmount(new BigDecimal("99.99"));
    variant.setPriceCurrency("USD");
    variant.setSystemGenerated(false);

    var saved = em.persistAndFlush(variant);
    assertThat(saved.getId()).isNotNull();

    var variantId = saved.getId();

    // Delete the variant
    variantRepository.deleteById(variantId);
    em.flush();

    // Verify it was deleted
    var deleted = em.find(ProductVariantEntity.class, variantId);
    assertThat(deleted).isNull();
  }

  @Test
  void shouldDeleteAllVariantsByProductId() {
    var productId = UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858"); // Data Dynamo with 4 variants

    // Verify variants exist before deletion
    var countBefore = variantRepository.countByProductId(productId);
    assertThat(countBefore).isEqualTo(4L);

    // Delete all variants for the product
    variantRepository.deleteByProductId(productId);
    em.flush();

    // Verify all variants were deleted
    var countAfter = variantRepository.countByProductId(productId);
    assertThat(countAfter).isZero();
  }

  @Test
  void shouldFindSystemGeneratedVariantByProductId() {
    var productId = UUID.fromString("632a34d9-13fb-47f7-a324-d0e6ee160858");
    var found = variantRepository.findByProductIdAndSystemGenerated(productId);

    assertThat(found).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("sku", "DD-DEFAULT")
        .hasFieldOrPropertyWithValue("systemGenerated", true);
  }

  @Test
  void shouldReturnEmptyWhenNoSystemGeneratedVariantExists() {
    // Create a new product with only non-system-generated variants
    var product = new ProductEntity();
    product.setTitle("Test Product");
    product.setSlug("test-product");
    var savedProduct = em.persistAndFlush(product);

    var variant = new ProductVariantEntity();
    variant.setProduct(savedProduct);
    variant.setSku("TEST-MANUAL");
    variant.setTitle("Manual Variant");
    variant.setPriceAmount(new BigDecimal("49.99"));
    variant.setPriceCurrency("USD");
    variant.setSystemGenerated(false);
    em.persistAndFlush(variant);

    var found = variantRepository.findByProductIdAndSystemGenerated(savedProduct.getId());

    assertThat(found).isEmpty();
  }

  @Test
  void shouldHandleVariantsWithDifferentCurrencies() {
    var variantId = UUID.fromString("e5f6a7b8-1111-2222-3333-444455556666"); // Pixel Forge with EUR
    var found = variantRepository.findById(variantId);

    assertThat(found).isPresent()
        .get()
        .hasFieldOrPropertyWithValue("priceCurrency", "EUR")
        .hasFieldOrPropertyWithValue("priceAmount", new BigDecimal("179.9900"));
  }
}
