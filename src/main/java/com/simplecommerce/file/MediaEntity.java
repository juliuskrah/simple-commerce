package com.simplecommerce.file;

import com.simplecommerce.product.ProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Auditable;

/**
 * @author julius.krah
 */
@Entity(name = "Media")
@Table(name = "media")
public class MediaEntity implements Auditable<String, UUID, OffsetDateTime> {
  @Id
  @GeneratedValue
  private UUID id;
  @ManyToOne
  private ProductEntity product;
  private URL url;
  private String contentType;
  @CreationTimestamp
  @Column(nullable = false)
  private OffsetDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return null == id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ProductEntity getProduct() {
    return product;
  }

  public void setProduct(ProductEntity product) {
    this.product = product;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MediaEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "MediaEntity{" +
        "id=" + id +
        ", url=" + url +
        ", contentType='" + contentType + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", createdBy='" + createdBy + '\'' +
        ", updatedBy='" + updatedBy + '\'' +
        '}';
  }

  @Override
  public Optional<String> getCreatedBy() {
    return Optional.ofNullable(createdBy);
  }

  @Override
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public Optional<OffsetDateTime> getCreatedDate() {
    return Optional.ofNullable(createdAt);
  }

  @Override
  public void setCreatedDate(OffsetDateTime creationDate) {
    this.createdAt = creationDate;
  }

  @Override
  public Optional<String> getLastModifiedBy() {
    return Optional.ofNullable(updatedBy);
  }

  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.updatedBy = lastModifiedBy;
  }

  @Override
  public Optional<OffsetDateTime> getLastModifiedDate() {
    return Optional.ofNullable(updatedAt);
  }

  @Override
  public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
    this.updatedAt = lastModifiedDate;
  }
}
