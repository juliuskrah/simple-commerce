package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.NotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ProductVariantManagement.
 * Note: These tests focus on the core logic without virtual thread execution.
 */
@ExtendWith(MockitoExtension.class)
class ProductVariantManagementTest {

  @Mock
  private ProductVariants variantRepository;

  private ProductVariantManagement variantManagement;

  @BeforeEach
  void setUp() {
    variantManagement = new ProductVariantManagement();
    variantManagement.setVariantRepositoryDirect(variantRepository);
  }

  @Test
  void shouldCreateVariantWithoutSystemGeneratedVariant() {
    // Given
    var productId = UUID.randomUUID();
    var globalProductId = new GlobalId("Product", productId.toString()).encode();
    when(variantRepository.findByProductIdAndSystemGenerated(productId, true))
        .thenReturn(Optional.empty());
    when(variantRepository.saveAndFlush(any(ProductVariantEntity.class))).thenAnswer(invocation -> {
      ProductVariantEntity variantEntity = invocation.getArgument(0);
      // We need to set the ID to simulate a saved entity
      variantEntity.setId(UUID.randomUUID());
      return null; // don't care about the return value
    });

    // When
    var input = new ProductVariantInput(
        "TEST-SKU-001",
        "Test Variant",
        new MoneyInput(new BigDecimal("19.99"), "USD")
    );
    var result = variantManagement.createVariant(globalProductId, input);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.sku()).isEqualTo("TEST-SKU-001");
    assertThat(result.title()).isEqualTo("Test Variant");
    assertThat(result.priceAmount()).isEqualTo(new BigDecimal("19.99"));
    assertThat(result.priceCurrency()).isEqualTo("USD");
    verify(variantRepository).saveAndFlush(any(ProductVariantEntity.class));
  }

  @Test
  void shouldThrowNotFoundExceptionWhenUpdatingNonExistentVariant() {
    // Given
    var variantId = UUID.randomUUID();
    var globalVariantId = new GlobalId("ProductVariant", variantId.toString()).encode();

    when(variantRepository.findById(variantId)).thenReturn(Optional.empty());

    // When & Then
    var input = new ProductVariantInput("SKU", "Title", null);
    assertThatThrownBy(() -> variantManagement.updateVariant(globalVariantId, input))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void shouldPreventDeletingLastVariant() {
    // Given
    var variantId = UUID.randomUUID();
    var globalVariantId = new GlobalId("ProductVariant", variantId.toString()).encode();
    
    var variant = new ProductVariantEntity();
    variant.setId(variantId);
    var product = new ProductEntity();
    var productId = UUID.randomUUID();
    product.setId(productId);
    variant.setProduct(product);
    
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variant));
    when(variantRepository.countByProductId(productId)).thenReturn(1L);

    // When & Then
    assertThatThrownBy(() -> variantManagement.deleteVariant(globalVariantId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot delete the last variant of a product");
  }

  @Test
  void shouldDeleteVariantWhenMultipleExist() {
    // Given
    var variantId = UUID.randomUUID();
    var globalVariantId = new GlobalId("ProductVariant", variantId.toString()).encode();
    
    var variant = new ProductVariantEntity();
    variant.setId(variantId);
    var product = new ProductEntity();
    var productId = UUID.randomUUID();
    product.setId(productId);
    variant.setProduct(product);
    
    when(variantRepository.findById(variantId)).thenReturn(Optional.of(variant));
    when(variantRepository.countByProductId(productId)).thenReturn(3L);

    // When
    var result = variantManagement.deleteVariant(globalVariantId);

    // Then
    assertThat(result).isEqualTo(variantId.toString());
    verify(variantRepository).deleteById(variantId);
  }
}
