package com.simplecommerce.file;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Repository for digital content.
 *
 * @author julius.krah
 */
public interface DigitalContents extends Repository<DigitalContentEntity, UUID> {

  Optional<DigitalContentEntity> findById(UUID id);

  Optional<DigitalContentEntity> findByVariantId(UUID variantId);

  DigitalContentEntity saveAndFlush(DigitalContentEntity entity);

  void deleteById(UUID id);
}
