package com.simplecommerce.discount;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link DiscountEntity}.
 *
 * @author julius.krah
 */
public interface Discounts extends JpaRepository<DiscountEntity, UUID> {

  /**
   * Find a discount by code.
   *
   * @param code the discount code
   * @return the discount if found
   */
  Optional<DiscountEntity> findByCodeIgnoreCase(String code);

  /**
   * Count how many times a customer has used a discount.
   *
   * @param discountId the discount ID
   * @param customerId the customer ID
   * @return the usage count
   */
  @Query("""
      SELECT COUNT(du) FROM DiscountUsage du
      WHERE du.discount.id = :discountId
        AND du.customer.id = :customerId
      """)
  long countCustomerUsage(
      @Param("discountId") UUID discountId,
      @Param("customerId") UUID customerId
  );
}
