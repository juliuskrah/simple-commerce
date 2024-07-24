package com.simplecommerce.product;

import com.simplecommerce.shared.DomainEvent;

/**
 * @author julius.krah
 */
public record ProductCreated(ProductEntity product) implements DomainEvent {

}
