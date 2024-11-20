package com.simplecommerce.product;

import com.simplecommerce.shared.Money;
import java.time.OffsetDateTime;
import java.util.List;
import javax.money.MonetaryAmount;

/**
 * @author julius.krah
 */
record PriceSet(
    String id,
    OffsetDateTime createdAt,
    List<Money> prices,
    OffsetDateTime updatedAt
) {

}
