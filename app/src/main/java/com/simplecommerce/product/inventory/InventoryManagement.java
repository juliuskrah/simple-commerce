package com.simplecommerce.product.inventory;

import com.simplecommerce.product.variant.ProductVariants;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.utils.SecurityUtils;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of inventory management service.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class InventoryManagement implements InventoryService {

  private static final Logger LOG = LoggerFactory.getLogger(InventoryManagement.class);

  private ProductVariants variantRepository;
  private InventoryAdjustments adjustmentRepository;

  public void setVariantRepository(ObjectFactory<ProductVariants> variantRepository) {
    this.variantRepository = variantRepository.getObject();
  }

  public void setAdjustmentRepository(ObjectFactory<InventoryAdjustments> adjustmentRepository) {
    this.adjustmentRepository = adjustmentRepository.getObject();
  }

  @Override
  public int adjustInventory(String variantId, int adjustment, String reason) {
    LOG.debug("Adjusting inventory for variant {}: adjustment={}, reason={}", variantId, adjustment, reason);

    var gid = GlobalId.decode(variantId);
    var variant = variantRepository.findById(UUID.fromString(gid.id()))
        .orElseThrow(() -> new NotFoundException("Product variant not found"));

    if (!variant.getTrackInventory()) {
      throw new IllegalStateException("Cannot adjust inventory when tracking is disabled");
    }

    int previousQuantity = variant.getAvailableQuantity() != null ? variant.getAvailableQuantity() : 0;
    int newQuantity = variant.adjustQuantity(adjustment);

    // Create audit record
    var adjustmentEntity = new InventoryAdjustmentEntity();
    adjustmentEntity.setVariant(variant);
    adjustmentEntity.setAdjustment(adjustment);
    adjustmentEntity.setPreviousQuantity(previousQuantity);
    adjustmentEntity.setNewQuantity(newQuantity);
    adjustmentEntity.setReason(reason);
    adjustmentEntity.setAdjustedBy(SecurityUtils.getCurrentUserLogin().orElse("system"));

    adjustmentRepository.save(adjustmentEntity);
    variantRepository.saveAndFlush(variant);

    LOG.info("Inventory adjusted for variant {}: {} -> {} ({})", variantId, previousQuantity, newQuantity, reason);
    return newQuantity;
  }

  @Override
  public boolean setInventoryTracking(String variantId, boolean trackInventory, Integer initialQuantity) {
    LOG.debug("Setting inventory tracking for variant {}: trackInventory={}, initialQuantity={}",
        variantId, trackInventory, initialQuantity);

    var gid = GlobalId.decode(variantId);
    var variant = variantRepository.findById(UUID.fromString(gid.id()))
        .orElseThrow(() -> new NotFoundException("Product variant not found"));

    if (trackInventory && initialQuantity == null) {
      throw new IllegalArgumentException("Initial quantity must be provided when enabling tracking");
    }

    variant.setTrackInventory(trackInventory);
    if (trackInventory) {
      variant.setAvailableQuantity(initialQuantity);

      // Create audit record for initial inventory
      var adjustmentEntity = new InventoryAdjustmentEntity();
      adjustmentEntity.setVariant(variant);
      adjustmentEntity.setAdjustment(initialQuantity);
      adjustmentEntity.setPreviousQuantity(0);
      adjustmentEntity.setNewQuantity(initialQuantity);
      adjustmentEntity.setReason("INVENTORY_TRACKING_ENABLED");
      adjustmentEntity.setAdjustedBy(SecurityUtils.getCurrentUserLogin().orElse("system"));

      adjustmentRepository.save(adjustmentEntity);
    } else {
      variant.setAvailableQuantity(null);
    }

    variantRepository.saveAndFlush(variant);
    LOG.info("Inventory tracking {} for variant {}", trackInventory ? "enabled" : "disabled", variantId);
    return trackInventory;
  }

  @Override
  public boolean reserveInventory(String variantId, int quantity) {
    LOG.debug("Reserving inventory for variant {}: quantity={}", variantId, quantity);

    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }

    var gid = GlobalId.decode(variantId);
    var variant = variantRepository.findById(UUID.fromString(gid.id()))
        .orElseThrow(() -> new NotFoundException("Product variant not found"));

    if (!variant.getTrackInventory()) {
      LOG.debug("Inventory tracking disabled for variant {}, reservation always succeeds", variantId);
      return true;
    }

    if (!hasAvailableInventory(variantId, quantity)) {
      LOG.warn("Insufficient inventory for variant {}: requested={}, available={}",
          variantId, quantity, variant.getAvailableQuantity());
      throw new IllegalStateException("Insufficient inventory available");
    }

    int previousQuantity = variant.getAvailableQuantity();
    variant.adjustQuantity(-quantity);

    // Create audit record
    var adjustmentEntity = new InventoryAdjustmentEntity();
    adjustmentEntity.setVariant(variant);
    adjustmentEntity.setAdjustment(-quantity);
    adjustmentEntity.setPreviousQuantity(previousQuantity);
    adjustmentEntity.setNewQuantity(variant.getAvailableQuantity());
    adjustmentEntity.setReason("RESERVED");
    adjustmentEntity.setAdjustedBy(SecurityUtils.getCurrentUserLogin().orElse("system"));

    adjustmentRepository.save(adjustmentEntity);
    variantRepository.saveAndFlush(variant);

    LOG.info("Reserved {} units for variant {}: {} -> {}",
        quantity, variantId, previousQuantity, variant.getAvailableQuantity());
    return true;
  }

  @Override
  public void releaseInventory(String variantId, int quantity) {
    LOG.debug("Releasing inventory for variant {}: quantity={}", variantId, quantity);

    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }

    var gid = GlobalId.decode(variantId);
    var variant = variantRepository.findById(UUID.fromString(gid.id()))
        .orElseThrow(() -> new NotFoundException("Product variant not found"));

    if (!variant.getTrackInventory()) {
      LOG.debug("Inventory tracking disabled for variant {}, no release needed", variantId);
      return;
    }

    int previousQuantity = variant.getAvailableQuantity();
    variant.adjustQuantity(quantity);

    // Create audit record
    var adjustmentEntity = new InventoryAdjustmentEntity();
    adjustmentEntity.setVariant(variant);
    adjustmentEntity.setAdjustment(quantity);
    adjustmentEntity.setPreviousQuantity(previousQuantity);
    adjustmentEntity.setNewQuantity(variant.getAvailableQuantity());
    adjustmentEntity.setReason("RELEASED");
    adjustmentEntity.setAdjustedBy(SecurityUtils.getCurrentUserLogin().orElse("system"));

    adjustmentRepository.save(adjustmentEntity);
    variantRepository.saveAndFlush(variant);

    LOG.info("Released {} units for variant {}: {} -> {}",
        quantity, variantId, previousQuantity, variant.getAvailableQuantity());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasAvailableInventory(String variantId, int quantity) {
    var gid = GlobalId.decode(variantId);
    var variant = variantRepository.findById(UUID.fromString(gid.id()))
        .orElseThrow(() -> new NotFoundException("Product variant not found"));

    if (!variant.getTrackInventory()) {
      return true; // Always available if not tracking
    }

    return variant.getAvailableQuantity() != null && variant.getAvailableQuantity() >= quantity;
  }
}
