package com.simplecommerce.shared;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Wrapper around {@link ApplicationEventPublisher} to mimic the behavior of a
 * CDI event.
 * @author julius.krah
 */
@Component
public class Event<EVENT extends DomainEvent<?, ? extends Enum<?>>> {
  private final ApplicationEventPublisher publisher;

  public Event(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void fire(EVENT event) {
    publisher.publishEvent(event);
  }
}
