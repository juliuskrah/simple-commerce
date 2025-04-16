package com.simplecommerce.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class StateMachineListener extends StateMachineListenerAdapter<States, Events> {
  private static final Logger LOG = LoggerFactory.getLogger(StateMachineListener.class);

  @Override
  public void stateChanged(State<States, Events> from, State<States, Events> to) {
    LOG.info("State change to {}", to.getId());
  }
}
