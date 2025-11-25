package com.simplecommerce.cart;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Repository interface for Cart entities.
 *
 * @author julius.krah
 */
interface Carts extends Repository<CartEntity, UUID> {

  Optional<CartEntity> findById(UUID id);

  Optional<CartEntity> findByCustomerId(UUID customerId);

  Optional<CartEntity> findBySessionId(String sessionId);

  CartEntity save(CartEntity cart);

  void delete(CartEntity cart);

  void deleteById(UUID id);
}
