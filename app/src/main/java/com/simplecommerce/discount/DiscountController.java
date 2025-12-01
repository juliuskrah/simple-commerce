package com.simplecommerce.discount;

import com.simplecommerce.shared.types.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.function.Supplier;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * Controller for discount operations.
 *
 * @author julius.krah
 * @since 1.0
 */
@Controller
class DiscountController {

  private static final Logger LOG = LoggerFactory.getLogger(DiscountController.class);

  private final ObjectProvider<DiscountService> discountService;
  private final Supplier<DiscountService> discountServiceSupplier =
      SingletonSupplier.of(DiscountManagement::new);

  DiscountController(ObjectProvider<DiscountService> discountService) {
    this.discountService = discountService;
  }

  @QueryMapping
  DiscountValidation validateDiscountCode(
      @Argument String code,
      @Argument BigDecimal orderAmount,
      @Argument Currency currency) {

    LOG.debug("Validating discount code: code={}, amount={}, currency={}",
        code, orderAmount, currency);

    // For now, we don't have customer context in unauthenticated flow
    // Pass null for customerId - the service will skip per-customer limit checks
    var result = discountService.getIfAvailable(discountServiceSupplier)
        .validateAndApplyDiscount(code, null, orderAmount);

    if (!result.valid()) {
      return new DiscountValidation(
          false,
          null,
          result.errorMessage(),
          null
      );
    }

    // Convert discount entity to GraphQL record
    var discountEntity = result.discount();
    CurrencyUnit currencyUnit = Monetary.getCurrency(currency.getCurrencyCode());
    Money valueAmount = null;
    Money minimumOrderAmount = null;

    if (discountEntity.getValueAmount() != null) {
      valueAmount = new Money(currencyUnit, discountEntity.getValueAmount());
    }

    if (discountEntity.getMinimumOrderAmount() != null) {
      minimumOrderAmount = new Money(currencyUnit, discountEntity.getMinimumOrderAmount());
    }

    var discount = new Discount(
        discountEntity.getCode(),
        discountEntity.getName(),
        discountEntity.getDescription(),
        discountEntity.getDiscountType(),
        discountEntity.getValuePercentage(),
        valueAmount,
        minimumOrderAmount,
        discountEntity.getUsageCount(),
        discountEntity.getUsageLimit(),
        discountEntity.getPerCustomerLimit(),
        discountEntity.getStartsAt(),
        discountEntity.getEndsAt(),
        discountEntity.getActive()
    );

    Money discountAmount = new Money(currencyUnit, result.discountAmount());

    return new DiscountValidation(
        true,
        discountAmount,
        null,
        discount
    );
  }
}
