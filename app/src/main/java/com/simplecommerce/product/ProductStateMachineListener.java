package com.simplecommerce.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Listener for Product state machine events.
 * Handles logging and additional business logic when state transitions occur.
 * 
 * @author julius.krah
 * @since 1.0
 */
public class ProductStateMachineListener extends StateMachineListenerAdapter<ProductState, ProductStateMachineEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(ProductStateMachineListener.class);

  @Override
  public void stateChanged(State<ProductState, ProductStateMachineEvent> from, 
                          State<ProductState, ProductStateMachineEvent> to) {
    if (from != null && to != null) {
      LOG.info("Product state changed from {} to {}", from.getId(), to.getId());
    } else if (to != null) {
      LOG.info("Product state machine started with initial state: {}", to.getId());
    }
  }

  @Override
  public void transition(Transition<ProductState, ProductStateMachineEvent> transition) {
    LOG.debug("Product state transition: {} -> {} on event {}", 
        transition.getSource().getId(),
        transition.getTarget().getId(),
        transition.getTrigger().getEvent());
  }

  @Override
  public void transitionStarted(Transition<ProductState, ProductStateMachineEvent> transition) {
    LOG.debug("Product state transition started: {} -> {} on event {}", 
        transition.getSource().getId(),
        transition.getTarget().getId(),
        transition.getTrigger().getEvent());
  }

  @Override
  public void transitionEnded(Transition<ProductState, ProductStateMachineEvent> transition) {
    LOG.debug("Product state transition ended: {} -> {} on event {}", 
        transition.getSource().getId(),
        transition.getTarget().getId(),
        transition.getTrigger().getEvent());
  }
}