package com.simplecommerce.product;

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
  void on(ProductCreated event) {
    LOG.info("Product created: {}", event.product());
  }
}
