package com.simplecommerce.cart;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Repository interface for CartItem entities.
 *
 * @author julius.krah
 */
interface CartItems extends Repository<CartItemEntity, UUID> {

  Optional<CartItemEntity> findById(UUID id);

  CartItemEntity save(CartItemEntity cartItem);

  void delete(CartItemEntity cartItem);

  void deleteById(UUID id);
}
