package com.simplecommerce.actor;

/**
 * Defines the different types of actors in the system.
 * Each actor type has different capabilities and permissions.
 *
 * @author julius.krah
 * @since 1.0
 */
public enum ActorType {
  /**
   * Staff members who can manage the admin dashboard, products, orders, etc.
   */
  STAFF,

  /**
   * Customers who can interact with the storefront and start checkout flows.
   */
  CUSTOMER,

  /**
   * Bots tied to automation and external applications for system integration.
   */
  BOT
}