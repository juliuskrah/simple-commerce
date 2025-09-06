package com.simplecommerce.product;

import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.action.Action;

/**
 * Configuration for the Product lifecycle state machine.
 * 
 * State Transitions:
 * DRAFT → PUBLISHED (PUBLISH event with validation guards)
 * PUBLISHED → ARCHIVED (ARCHIVE event)
 * ARCHIVED → DRAFT (REACTIVATE event)
 * 
 * @author julius.krah
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class ProductStateMachineConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(ProductStateMachineConfiguration.class);

  /**
   * Creates a state machine factory for Product lifecycle management.
   * Using factory pattern allows us to create multiple state machine instances
   * for different products.
   */
  @Bean
  public StateMachine<ProductState, ProductStateMachineEvent> productStateMachine() throws Exception {
    Builder<ProductState, ProductStateMachineEvent> builder = StateMachineBuilder.builder();

    builder.configureConfiguration()
        .withConfiguration()
        .autoStartup(false)  // We'll start it manually when needed
        .listener(new ProductStateMachineListener());

    builder.configureStates()
        .withStates()
        .initial(ProductState.DRAFT)
        .states(EnumSet.allOf(ProductState.class));

    builder.configureTransitions()
        // DRAFT → PUBLISHED (with validation guard)
        .withExternal()
        .source(ProductState.DRAFT)
        .target(ProductState.PUBLISHED)
        .event(ProductStateMachineEvent.PUBLISH)
        .guard(publishGuard())
        .action(publishAction())
        .and()
        
        // PUBLISHED → ARCHIVED
        .withExternal()
        .source(ProductState.PUBLISHED)
        .target(ProductState.ARCHIVED)
        .event(ProductStateMachineEvent.ARCHIVE)
        .action(archiveAction())
        .and()
        
        // ARCHIVED → DRAFT (reactivation)
        .withExternal()
        .source(ProductState.ARCHIVED)
        .target(ProductState.DRAFT)
        .event(ProductStateMachineEvent.REACTIVATE)
        .action(reactivateAction());

    return builder.build();
  }

  /**
   * Guard to validate if a product can be published.
   * Checks business rules before allowing DRAFT → PUBLISHED transition.
   */
  @Bean
  public Guard<ProductState, ProductStateMachineEvent> publishGuard() {
    return context -> {
      // Get the product from the state machine context
      ProductEntity product = context.getExtendedState().get("product", ProductEntity.class);
      
      if (product == null) {
        LOG.warn("No product found in state machine context");
        return false;
      }

      LOG.debug("Validating product {} for publishing", product.getId());

      // Validation 1: Product must have title
      if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
        LOG.warn("Product {} cannot be published: missing title", product.getId());
        return false;
      }

      // Validation 2: Product must have at least one variant with pricing
      // This would need to be implemented to check variants from database
      // For now, we'll assume this validation passes
      
      LOG.info("Product {} passed validation for publishing", product.getId());
      return true;
    };
  }

  /**
   * Action executed when product is published.
   */
  @Bean
  public Action<ProductState, ProductStateMachineEvent> publishAction() {
    return context -> {
      ProductEntity product = context.getExtendedState().get("product", ProductEntity.class);
      if (product != null) {
        LOG.info("Publishing product {}: {}", product.getId(), product.getTitle());
        // Here you could trigger additional business logic like:
        // - Indexing in search engine
        // - Notifying marketing team
        // - Updating cache
        // - Sending notifications
      }
    };
  }

  /**
   * Action executed when product is archived.
   */
  @Bean
  public Action<ProductState, ProductStateMachineEvent> archiveAction() {
    return context -> {
      ProductEntity product = context.getExtendedState().get("product", ProductEntity.class);
      if (product != null) {
        LOG.info("Archiving product {}: {}", product.getId(), product.getTitle());
        // Here you could trigger additional business logic like:
        // - Removing from search indices
        // - Handling active orders
        // - Notifying customers with saved items
        // - Updating inventory systems
      }
    };
  }

  /**
   * Action executed when product is reactivated from archive.
   */
  @Bean
  public Action<ProductState, ProductStateMachineEvent> reactivateAction() {
    return context -> {
      ProductEntity product = context.getExtendedState().get("product", ProductEntity.class);
      if (product != null) {
        LOG.info("Reactivating product {}: {}", product.getId(), product.getTitle());
        // Reset product to draft state for re-validation
        // Could trigger review workflow
      }
    };
  }
}