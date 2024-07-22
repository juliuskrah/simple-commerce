package com.simplecommerce.shared;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Wrapper around {@link ApplicationEventPublisher} to mimic the behavior of a
 * CDI event.
 * @author julius.krah
 */
@Component
public class Event<E extends DomainEvent> {
  private final ApplicationEventPublisher publisher;

  public Event(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void fire(E event) {
    publisher.publishEvent(event);
  }
}
