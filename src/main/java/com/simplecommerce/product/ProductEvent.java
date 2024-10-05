package com.simplecommerce.product;

import com.simplecommerce.product.ProductEvent.ProductEventType;
import com.simplecommerce.shared.DomainEvent;

/**
 * @author julius.krah
 */
record ProductEvent(ProductEntity source, ProductEventType eventType)
    implements DomainEvent<ProductEntity, ProductEventType> {
  enum ProductEventType {
    CREATED,
    UPDATED,
  }
}
