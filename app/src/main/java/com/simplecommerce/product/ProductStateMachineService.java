package com.simplecommerce.product;

import com.simplecommerce.shared.types.Product;
import com.simplecommerce.shared.types.ProductState;
import com.simplecommerce.shared.types.ProductStateMachineEvent;
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
   * @return emit new status if the transition was successful, old status otherwise
   */
  public Mono<ProductStatus> publishProduct(Product product) {
    LOG.info("Attempting to publish product {}: {}", product.id(), product.title());
    return executeTransition(product, ProductStateMachineEvent.PUBLISH);
  }

  /**
   * Archives a product by transitioning from PUBLISHED to ARCHIVED state.
   * 
   * @param product the product to archive
   * @return emit new status if the transition was successful, old status otherwise
   */
  public Mono<ProductStatus> archiveProduct(Product product) {
    LOG.info("Attempting to archive product {}: {}", product.id(), product.title());
    return executeTransition(product, ProductStateMachineEvent.ARCHIVE);
  }

  /**
   * Reactivates a product by transitioning from ARCHIVED to DRAFT state.
   * 
   * @param product the product to reactivate
   * @return emit new status if the transition was successful, old status otherwise
   */
  public Mono<ProductStatus> reactivateProduct(Product product) {
    LOG.info("Attempting to reactivate product {}: {}", product.id(), product.title());
    return executeTransition(product, ProductStateMachineEvent.REACTIVATE);
  }

  private Mono<ProductStatus> executeTransition(Product product, ProductStateMachineEvent event) {

    var currentState = mapStatusToState(product.status());
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
          LOG.info("Product {} successfully transitioned to state: {}", product.id(), newState);
          return newStatus;
        })
        .onErrorReturn(ex -> {
          LOG.error("Error executing state transition for product {}. Old state maintained", product.id(), ex);
          return ex instanceof Exception;
        }, product.status())
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