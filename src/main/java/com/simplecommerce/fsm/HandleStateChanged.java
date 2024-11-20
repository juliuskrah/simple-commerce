package com.simplecommerce.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.OnStateChanged;
import org.springframework.statemachine.annotation.WithStateMachine;

/**
 * This class is used to handle state changes in the state machine.
 * @author julius.krah
 */
@WithStateMachine
public class HandleStateChanged {
  private static final Logger LOG = LoggerFactory.getLogger(HandleStateChanged.class);

  // Method's access modifier is public to satisfy the requirements of Spring Expression Language
  @OnStateChanged
  public void stateChanges(StateContext<States, Events> context) {
    LOG.info("State change to {}", context.getTarget().getId());
  }
}
