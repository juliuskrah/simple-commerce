package com.simplecommerce.shared.config;

import com.simplecommerce.fsm.Events;
import com.simplecommerce.fsm.States;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 * @author julius.krah
 */
@EnableStateMachine
@Configuration(proxyBeanMethods = false)
public class StateMachineConfiguration extends EnumStateMachineConfigurerAdapter<States, Events> {

  private static final Logger LOG = LoggerFactory.getLogger(StateMachineConfiguration.class);

  private StateMachineListener<States, Events> listener() {
    return new StateMachineListenerAdapter<>() {
      @Override
      public void stateChanged(State<States, Events> from, State<States, Events> to) {
        LOG.info("State change to {}", to.getId());
      }
    };
  }

  @Override
  public void configure(StateMachineConfigurationConfigurer<States, Events> config)
      throws Exception {
    config
        .withConfiguration()
        .autoStartup(true)
        .listener(listener());
  }

  @Override
  public void configure(StateMachineStateConfigurer<States, Events> states)
      throws Exception {
    states
        .withStates()
        .initial(States.SI)
        .states(EnumSet.allOf(States.class));
  }

  @Override
  public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
      throws Exception {
    transitions
        .withExternal()
        .source(States.SI).target(States.S1).event(Events.E1)
        .and()
        .withExternal()
        .source(States.S1).target(States.S2).event(Events.E2);
  }
}
