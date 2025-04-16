package com.simplecommerce.shared.config;

import com.simplecommerce.fsm.Events;
import com.simplecommerce.fsm.StateMachineListener;
import com.simplecommerce.fsm.States;
import java.util.EnumSet;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * @author julius.krah
 */
@EnableStateMachine
@Configuration(proxyBeanMethods = false)
public class StateMachineConfiguration extends EnumStateMachineConfigurerAdapter<States, Events> {

  @Override
  public void configure(StateMachineConfigurationConfigurer<States, Events> config)
      throws Exception {
    config
        .withConfiguration()
        .autoStartup(true)
        .listener(new StateMachineListener());
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
