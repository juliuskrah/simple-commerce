package com.simplecommerce.actor;

import com.simplecommerce.node.Node;
import java.time.OffsetDateTime;

/**
 * Base interface for all actors in the system.
 * An Actor is an entity that can perform actions in the system.
 * This includes Staff (admin users), Customers (storefront users), and Bots (automation).
 *
 * @since 1.0
 * @author julius.krah
 */
public sealed interface Actor extends Node permits User, Bot {
  @Override
  String id();
  String username();
  OffsetDateTime createdAt();
  OffsetDateTime updatedAt();
}
