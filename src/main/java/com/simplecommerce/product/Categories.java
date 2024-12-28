package com.simplecommerce.product;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

interface Categories extends Repository<CategoryEntity, UUID> {
  CategoryEntity saveAndFlush(CategoryEntity category);

  Optional<CategoryEntity> findById(UUID id);
}
