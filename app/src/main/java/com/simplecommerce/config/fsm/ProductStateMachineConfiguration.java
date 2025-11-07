package com.simplecommerce.config.fsm;

import com.simplecommerce.shared.types.Product;
import com.simplecommerce.shared.types.ProductState;
import com.simplecommerce.shared.types.ProductStateMachineEvent;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableWithStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.action.Action;

/**
 * Configuration for the Product lifecycle state machine.
 * State Transitions:
 * DRAFT → PUBLISHED (PUBLISH event with validation guards)
 * PUBLISHED → ARCHIVED (ARCHIVE event)
 * ARCHIVED → DRAFT (REACTIVATE event)
 * 
 * @author julius.krah
 * @since 1.0
 */
@EnableWithStateMachine
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
        .autoStartup(false);  // We'll start it manually when needed

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
  public Guard<ProductState, ProductStateMachineEvent> publishGuard() {
    return context -> {
      // Get the product from the state machine context
      Product product = context.getExtendedState().get("product", Product.class);
      
      if (product == null) {
        LOG.warn("No product found in state machine context");
        return false;
      }

      LOG.debug("Validating product {} for publishing", product.id());

      // Validation 1: Product must have title
      if (product.title() == null || product.title().isBlank()) {
        LOG.warn("Product {} cannot be published: missing title", product.id());
        return false;
      }

      // Validation 2: Product must have at least one variant with pricing
      // This would need to be implemented to check variants from database
      // For now, we'll assume this validation passes
      
      LOG.info("Product {} passed validation for publishing", product.id());
      return true;
    };
  }

  /**
   * Action executed when product is published.
   */
  public Action<ProductState, ProductStateMachineEvent> publishAction() {
    return context -> {
      Product product = context.getExtendedState().get("product", Product.class);
      if (product != null) {
        LOG.info("Publishing product {}: {}", product.id(), product.title());
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
  public Action<ProductState, ProductStateMachineEvent> archiveAction() {
    return context -> {
      Product product = context.getExtendedState().get("product", Product.class);
      if (product != null) {
        LOG.info("Archiving product {}: {}", product.id(), product.title());
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
  public Action<ProductState, ProductStateMachineEvent> reactivateAction() {
    return context -> {
      Product product = context.getExtendedState().get("product", Product.class);
      if (product != null) {
        LOG.info("Reactivating product {}: {}", product.id(), product.title());
        // Reset product to draft state for re-validation
        // Could trigger review workflow
      }
    };
  }
}