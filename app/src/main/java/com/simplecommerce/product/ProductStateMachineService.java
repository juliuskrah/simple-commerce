package com.simplecommerce.product;

import com.simplecommerce.shared.types.ProductStatus;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
   * @return emit true if the transition was successful, false otherwise
   */
  public Mono<Boolean> publishProduct(ProductEntity product) {
    LOG.info("Attempting to publish product {}: {}", product.getId(), product.getTitle());
    return executeTransition(product, ProductStateMachineEvent.PUBLISH);
  }

  /**
   * Archives a product by transitioning from PUBLISHED to ARCHIVED state.
   * 
   * @param product the product to archive
   * @return true if the transition was successful, false otherwise
   */
  public Mono<Boolean> archiveProduct(ProductEntity product) {
    LOG.info("Attempting to archive product {}: {}", product.getId(), product.getTitle());
    return executeTransition(product, ProductStateMachineEvent.ARCHIVE);
  }

  /**
   * Reactivates a product by transitioning from ARCHIVED to DRAFT state.
   * 
   * @param product the product to reactivate
   * @return true if the transition was successful, false otherwise
   */
  public Mono<Boolean> reactivateProduct(ProductEntity product) {
    LOG.info("Attempting to reactivate product {}: {}", product.getId(), product.getTitle());
    return executeTransition(product, ProductStateMachineEvent.REACTIVATE);
  }

  private Mono<Boolean> executeTransition(ProductEntity product, ProductStateMachineEvent event) {

    var currentState = mapStatusToState(product.getStatus());
    var context = new DefaultStateMachineContext<>(currentState, event, Map.of(), null);

    stateMachine.getStateMachineAccessor()
        .doWithAllRegions(access -> access.resetStateMachineReactively(context).subscribe());

    stateMachine.getExtendedState().getVariables().put("product", product);
    return stateMachine.startReactively()
        .then(
            stateMachine.sendEvent(
                Mono.just(MessageBuilder.withPayload(event).build())
            ).single()
        ).map(StateMachineEventResult::getResultType)
        .filter(resultType -> resultType == ResultType.ACCEPTED)
        .map(_ -> {
          var newState = stateMachine.getState().getId();
          var newStatus = mapStateToStatus(newState);
          product.setStatus(newStatus);
          LOG.info("Product {} successfully transitioned to state: {}", product.getId(), newState);
          return true;
        }).switchIfEmpty(Mono.just(false))
        .onErrorReturn(ex -> {
          LOG.error("Error executing state transition for product {}", product.getId(), ex);
          return ex instanceof Exception;
        }, false)
        .publishOn(Schedulers.boundedElastic())
        .doFinally(_ -> stateMachine.stopReactively().subscribe());
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

}