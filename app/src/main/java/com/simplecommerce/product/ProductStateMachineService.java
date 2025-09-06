package com.simplecommerce.product;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

/**
 * Service for managing Product state machine operations.
 * Handles state transitions, validation, and persistence of state changes.
 * 
 * @author julius.krah
 * @since 1.0
 */
@Service
public class ProductStateMachineService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductStateMachineService.class);
  
  private final StateMachine<ProductState, ProductStateMachineEvent> stateMachine;

  public ProductStateMachineService(StateMachine<ProductState, ProductStateMachineEvent> productStateMachine) {
    this.stateMachine = productStateMachine;
  }

  /**
   * Publishes a product by transitioning from DRAFT to PUBLISHED state.
   * 
   * @param product the product to publish
   * @return true if the transition was successful, false otherwise
   */
  public boolean publishProduct(ProductEntity product) {
    LOG.info("Attempting to publish product {}: {}", product.getId(), product.getTitle());
    
    if (product.getStatus() != ProductStatus.DRAFT) {
      LOG.warn("Product {} is not in DRAFT state, current state: {}", product.getId(), product.getStatus());
      return false;
    }

    return executeTransition(product, ProductStateMachineEvent.PUBLISH, ProductStatus.PUBLISHED);
  }

  /**
   * Archives a product by transitioning from PUBLISHED to ARCHIVED state.
   * 
   * @param product the product to archive
   * @return true if the transition was successful, false otherwise
   */
  public boolean archiveProduct(ProductEntity product) {
    LOG.info("Attempting to archive product {}: {}", product.getId(), product.getTitle());
    
    if (product.getStatus() != ProductStatus.PUBLISHED) {
      LOG.warn("Product {} is not in PUBLISHED state, current state: {}", product.getId(), product.getStatus());
      return false;
    }

    return executeTransition(product, ProductStateMachineEvent.ARCHIVE, ProductStatus.ARCHIVED);
  }

  /**
   * Reactivates a product by transitioning from ARCHIVED to DRAFT state.
   * 
   * @param product the product to reactivate
   * @return true if the transition was successful, false otherwise
   */
  public boolean reactivateProduct(ProductEntity product) {
    LOG.info("Attempting to reactivate product {}: {}", product.getId(), product.getTitle());
    
    if (product.getStatus() != ProductStatus.ARCHIVED) {
      LOG.warn("Product {} is not in ARCHIVED state, current state: {}", product.getId(), product.getStatus());
      return false;
    }

    return executeTransition(product, ProductStateMachineEvent.REACTIVATE, ProductStatus.DRAFT);
  }

  /**
   * Executes a state machine transition for the given product.
   * 
   * @param product the product entity
   * @param event the state machine event to trigger
   * @param expectedNewStatus the expected new status after transition
   * @return true if transition was successful, false otherwise
   */
  private boolean executeTransition(ProductEntity product, 
                                   ProductStateMachineEvent event, 
                                   ProductStatus expectedNewStatus) {
    try {
      // Create a unique state machine instance for this product
      var machineId = "product-" + product.getId();
      
      // Set the current state based on product status
      var currentState = mapStatusToState(product.getStatus());
      var context = new DefaultStateMachineContext<ProductState, ProductStateMachineEvent>(currentState, null, null, null);
      
      // Reset state machine to current product state
      stateMachine.getStateMachineAccessor()
          .doWithAllRegions(access -> access.resetStateMachine(context));
      
      // Store product in extended state for guards and actions
      stateMachine.getExtendedState().getVariables().put("product", product);
      
      // Start the state machine
      stateMachine.start();
      
      // Send the event
      boolean eventAccepted = stateMachine.sendEvent(event);
      
      if (eventAccepted) {
        // Get the new state
        var newState = stateMachine.getState().getId();
        var newStatus = mapStateToStatus(newState);
        
        // Update the product status
        product.setStatus(newStatus);
        
        LOG.info("Product {} successfully transitioned to state: {}", product.getId(), newState);
        return true;
      } else {
        LOG.warn("State machine rejected event {} for product {}", event, product.getId());
        return false;
      }
      
    } catch (Exception e) {
      LOG.error("Error executing state transition for product {}", product.getId(), e);
      return false;
    } finally {
      // Clean up
      stateMachine.stop();
    }
  }

  /**
   * Maps ProductStatus enum to ProductState enum.
   */
  private ProductState mapStatusToState(ProductStatus status) {
    return switch (status) {
      case DRAFT -> ProductState.DRAFT;
      case PUBLISHED -> ProductState.PUBLISHED;
      case ARCHIVED -> ProductState.ARCHIVED;
    };
  }

  /**
   * Maps ProductState enum to ProductStatus enum.
   */
  private ProductStatus mapStateToStatus(ProductState state) {
    return switch (state) {
      case DRAFT -> ProductStatus.DRAFT;
      case PUBLISHED -> ProductStatus.PUBLISHED;
      case ARCHIVED -> ProductStatus.ARCHIVED;
    };
  }

  /**
   * Validates if a product can be published based on business rules.
   * 
   * @param product the product to validate
   * @return true if product can be published, false otherwise
   */
  public boolean canPublish(ProductEntity product) {
    if (product.getStatus() != ProductStatus.DRAFT) {
      return false;
    }

    // Business rule validations
    if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
      LOG.debug("Product {} cannot be published: missing title", product.getId());
      return false;
    }

    // Additional validations could be added here:
    // - Check if product has variants with valid pricing
    // - Validate required product information
    // - Check inventory levels
    // - Validate product images/media
    
    return true;
  }

  /**
   * Gets the current state of the state machine for the given product.
   * 
   * @param product the product
   * @return the current product state
   */
  public ProductState getCurrentState(ProductEntity product) {
    return mapStatusToState(product.getStatus());
  }
}