package com.simplecommerce.fsm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author julius.krah
 */
@Component
public class StateMachineRunner implements CommandLineRunner {
  private final StateMachine<States, Events> stateMachine;

  public StateMachineRunner(StateMachine<States, Events> stateMachine) {
    this.stateMachine = stateMachine;
  }

  @Override
  public void run(String... args) {
    var result1 = stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E1).build()));
    var result12 = stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E2).build()));
    Flux.concat(result1, result12).subscribe();
  }

}
