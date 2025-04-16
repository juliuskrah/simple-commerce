package com.simplecommerce.file;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * @author julius.krah
 */
interface Media extends Repository<MediaEntity, UUID> {

  Optional<MediaEntity> findById(UUID id);

  @Query("SELECT m FROM Media m WHERE m.product.id = :productId")
  List<MediaEntity> findByProductId(UUID productId);

  MediaEntity saveAndFlush(MediaEntity media);
}
