package com.simplecommerce.product;

import com.simplecommerce.shared.types.Money;

/**
 * Represents a range of prices.
 *
 * @since 1.0
 * @author julius.krah
 */
public record PriceRange(Money start, Money stop) {

}
