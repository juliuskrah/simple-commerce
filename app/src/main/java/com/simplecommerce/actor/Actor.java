package com.simplecommerce.actor;

/**
 * Base interface for all actors in the system.
 * An Actor is an entity that can perform actions in the system.
 * This includes Staff (admin users), Customers (storefront users), and Bots (automation).
 *
 * @since 1.0
 * @author julius.krah
 */
public interface Actor {
  String id();
  String username();
  ActorType getActorType();
}
