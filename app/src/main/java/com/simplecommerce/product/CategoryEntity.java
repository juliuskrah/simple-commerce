package com.simplecommerce.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Auditable;

/**
 * @author julius.krah
 * @since 1.0
 */
@Entity(name = "Category")
@Table(name = "categories")
public class CategoryEntity implements Auditable<String, UUID, OffsetDateTime> {
  @Id
  @GeneratedValue
  private UUID id;
  private String title;
  @Column(unique = true, nullable = false)
  private String slug;
  private String description;
  @Column(columnDefinition = "ltree")
  private String path;
  @CreationTimestamp
  @Column(nullable = false)
  private OffsetDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNew() {
    return null == id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Optional<OffsetDateTime> getCreatedDate() {
    return Optional.ofNullable(createdAt);
  }

  @Override
  public void setCreatedDate(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public Optional<OffsetDateTime> getLastModifiedDate() {
    return Optional.ofNullable(updatedAt);
  }

  @Override
  public void setLastModifiedDate(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
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
  public Optional<String> getLastModifiedBy() {
    return Optional.ofNullable(updatedBy);
  }

  @Override
  public void setLastModifiedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CategoryEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public String toString() {
    return "CategoryEntity{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", slug='" + slug + '\'' +
        ", description='" + description + '\'' +
        ", path='" + path + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", createdBy='" + createdBy + '\'' +
        ", updatedBy='" + updatedBy + '\'' +
        '}';
  }

}
