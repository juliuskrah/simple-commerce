package com.simplecommerce.discount;

import com.simplecommerce.shared.GlobalId;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DiscountService.
 *
 * @author julius.krah
 */
@Service
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class DiscountManagement implements DiscountService {

  private static final Logger LOG = LoggerFactory.getLogger(DiscountManagement.class);

  private Discounts discountRepository;

  public void setDiscountRepository(ObjectFactory<Discounts> discountRepository) {
    this.discountRepository = discountRepository.getObject();
  }

  @Override
  @Transactional(readOnly = true)
  public DiscountResult validateAndApplyDiscount(
      String code,
      String customerId,
      BigDecimal orderAmount) {

    LOG.debug("Validating discount code: {}", code);

    var discount = discountRepository.findByCodeIgnoreCase(code);

    if (discount.isEmpty()) {
      return DiscountResult.invalid("Discount code not found");
    }

    var discountEntity = discount.get();

    // Check if discount is valid (active, within date range, not exceeded usage limit)
    if (!discountEntity.isValid()) {
      return DiscountResult.invalid("Discount code is not valid or has expired");
    }

    // Check minimum order amount
    if (discountEntity.getMinimumOrderAmount() != null &&
        orderAmount.compareTo(discountEntity.getMinimumOrderAmount()) < 0) {
      return DiscountResult.invalid(
          String.format("Minimum order amount of %s required",
              discountEntity.getMinimumOrderAmount()));
    }

    // Check per-customer usage limit
    if (discountEntity.getPerCustomerLimit() != null && customerId != null) {
      var globalId = GlobalId.decode(customerId);
      long customerUsage = discountRepository.countCustomerUsage(
          discountEntity.getId(),
          UUID.fromString(globalId.id())
      );

      if (customerUsage >= discountEntity.getPerCustomerLimit()) {
        return DiscountResult.invalid("You have reached the usage limit for this discount");
      }
    }

    // Calculate discount amount
    var discountAmount = discountEntity.calculateDiscount(orderAmount);

    LOG.info("Discount applied: code={}, amount={}", code, discountAmount);
    return DiscountResult.valid(discountAmount, discountEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateDiscountAmount(String code, BigDecimal orderAmount) {
    var discount = discountRepository.findByCodeIgnoreCase(code);

    if (discount.isEmpty() || !discount.get().isValid()) {
      return BigDecimal.ZERO;
    }

    return discount.get().calculateDiscount(orderAmount);
  }
}
