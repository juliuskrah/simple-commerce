package com.simplecommerce.product;

import com.simplecommerce.product.ProductEvent.ProductEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Component
class ProductEventListener {
  private static final Logger LOG = LoggerFactory.getLogger(ProductEventListener.class);

  @ApplicationModuleListener
  void on(ProductEvent event) {
    switch (event) {
      case ProductEvent(ProductEntity source, ProductEventType eventType) when (eventType == ProductEventType.CREATED) ->
          LOG.info("Product created : {}", source);
      case ProductEvent(ProductEntity source, ProductEventType eventType) when (eventType == ProductEventType.UPDATED) ->
          LOG.info("Product updated: {}", source);
      default -> LOG.info("Unmapped product event {}: {}", event.eventType(), event.source());
    }
  }
}
