package com.simplecommerce.product;

import com.simplecommerce.product.ProductEvent.ProductEventType;
import com.simplecommerce.shared.DomainEvent;

/**
 * Product events on the {@link ProductEntity product entity} that fires on:
 * <ul>
 *   <li>{@link ProductEventType#CREATED Product creation}</li>
 *   <li>{@link ProductEventType#UPDATED Product update}</li>
 * </ul>
 * @author julius.krah
 * @see ProductEntity#publishProductCreatedEvent()
 * @see ProductEventListener#on(ProductEvent)
 */
record ProductEvent(ProductEntity source, ProductEventType eventType)
    implements DomainEvent<ProductEntity, ProductEventType> {
  enum ProductEventType {
    CREATED,
    UPDATED,
    DELETED
  }
}
