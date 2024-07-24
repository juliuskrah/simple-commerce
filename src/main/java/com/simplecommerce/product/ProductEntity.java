package com.simplecommerce.product;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * @author julius.krah
 */
@Entity(name = "Product")
@Table(name = "products")
public class ProductEntity extends AbstractAggregateRoot<ProductEntity> {

  @Id
  @GeneratedValue
  private UUID id;

  private String title;

  @Column(unique = true, nullable = false)
  private String slug;

  private String description;

  @CreationTimestamp
  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @ElementCollection
  @CollectionTable(name = "tags", joinColumns = @JoinColumn(name = "type_id"))
  private List<String> tags = new ArrayList<>();

  void publishProductCreatedEvent() {
    registerEvent(new ProductCreated(this));
  }

  void addTags(String... tags) {
    this.tags.addAll(Arrays.stream(tags).toList());
  }

  void addTag(String tag) {
    tags.add(tag);
  }

  void removeTag(String tag) {
    tags.remove(tag);
  }

  public UUID getId() {
    return id;
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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProductEntity that)) {
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
    return "ProductEntity{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", slug='" + slug + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
