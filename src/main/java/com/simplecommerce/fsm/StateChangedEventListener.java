package com.simplecommerce.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.statemachine.event.OnStateChangedEvent;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Component
public class StateChangedEventListener implements ApplicationListener<OnStateChangedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(StateChangedEventListener.class);

  @Override
  public void onApplicationEvent(OnStateChangedEvent event) {
    LOG.info("State change to {}", event.getTargetState().getId());
  }
}
