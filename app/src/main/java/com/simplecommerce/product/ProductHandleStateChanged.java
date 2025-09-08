package com.simplecommerce.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.OnStateChanged;
import org.springframework.statemachine.annotation.WithStateMachine;

import com.simplecommerce.fsm.Events;
import com.simplecommerce.fsm.States;

/**
 * This class is used to handle state changes in the state machine.
 * @author julius.krah
 */
@WithStateMachine
public class ProductHandleStateChanged {
  private static final Logger LOG = LoggerFactory.getLogger(ProductHandleStateChanged.class);

  // Method's access modifier is public to satisfy the requirements of Spring Expression Language
  @OnStateChanged
  public void stateChanges(StateContext<States, Events> context) {
    if (context.getSource() != null && context.getTarget() != null) {
      LOG.info("Product state changed from {} to {}", context.getSource().getId(), context.getTarget().getId());
    } else if (context.getTarget() != null) {
      LOG.info("Product state machine started with initial state: {}", context.getTarget().getId());
    }
  }
}
