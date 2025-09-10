package com.simplecommerce.product.variant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.repository.Repository;

/**
 * Repository for product variants.
 * @author julius.krah
 */
public interface ProductVariants extends Repository<ProductVariantEntity, UUID> {
  
  Optional<ProductVariantEntity> findById(UUID id);
  
  Window<ProductVariantEntity> findByProductId(UUID productId, Limit limit, Sort sort, ScrollPosition scroll);
  
  long countByProductId(UUID productId);
  
  Optional<ProductVariantEntity> findBySku(String sku);
  
  ProductVariantEntity saveAndFlush(ProductVariantEntity entity);
  
  void deleteById(UUID id);
  
  Optional<ProductVariantEntity> findByProductIdAndSystemGenerated(UUID productId, Boolean systemGenerated);
}
