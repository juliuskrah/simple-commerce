package com.simplecommerce.product.inventory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.repository.Repository;

/**
 * Repository interface for inventory adjustment entities.
 *
 * @author julius.krah
 */
interface InventoryAdjustments extends Repository<InventoryAdjustmentEntity, UUID> {

  List<InventoryAdjustmentEntity> findByVariantIdOrderByCreatedAtDesc(UUID variantId, Limit limit);

  InventoryAdjustmentEntity save(InventoryAdjustmentEntity adjustment);
}
