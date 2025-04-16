package com.simplecommerce.product;

import com.simplecommerce.shared.Money;

/**
 * Represents a range of prices.
 *
 * @since 1.0
 * @author julius.krah
 */
record PriceRange(Money start, Money stop) {

}
