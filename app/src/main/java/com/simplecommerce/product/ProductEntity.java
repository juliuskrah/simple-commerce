package com.simplecommerce.product;

import static jakarta.persistence.FetchType.LAZY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.simplecommerce.product.ProductEvent.ProductEventType;
import com.simplecommerce.product.category.CategoryEntity;
import com.simplecommerce.shared.types.ProductStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Auditable;

/**
 * @author julius.krah
 */
@Entity(name = "Product")
@Table(name = "products")
public class ProductEntity extends AbstractAggregateRoot<ProductEntity> implements Auditable<String, UUID, OffsetDateTime> {
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
  private String createdBy;
  private String updatedBy;
  @ElementCollection
  @CollectionTable(name = "product_tag", joinColumns = @JoinColumn(name = "product_id"))
  private List<String> tags = new ArrayList<>();
  @JsonIgnore
  @ManyToOne(fetch = LAZY)
  private CategoryEntity category;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductStatus status = ProductStatus.DRAFT;

  /**
   * Publishes a {@link ProductEvent} of type {@link ProductEventType#CREATED}.
   */
  void publishProductCreatedEvent() {
    registerEvent(new ProductEvent(this, ProductEventType.CREATED));
  }

  void addTags(String... tags) {
    this.tags.addAll(Arrays.stream(tags).toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNew() {
    return null == id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getCreatedBy() {
    return Optional.ofNullable(createdBy);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getLastModifiedBy() {
    return Optional.ofNullable(updatedBy);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.updatedBy = lastModifiedBy;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<OffsetDateTime> getCreatedDate() {
    return Optional.ofNullable(createdAt);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCreatedDate(OffsetDateTime creationDate) {
    this.createdAt = creationDate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<OffsetDateTime> getLastModifiedDate() {
    return Optional.ofNullable(updatedAt);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
    this.updatedAt = lastModifiedDate;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public CategoryEntity getCategory() {
    return category;
  }

  public void setCategory(CategoryEntity category) {
    this.category = category;
  }

  public ProductStatus getStatus() {
    return status;
  }

  public void setStatus(ProductStatus status) {
    this.status = status;
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
        ", description='" + description + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", createdBy='" + createdBy + '\'' +
        ", updatedBy='" + updatedBy + '\'' +
        '}';
  }
}
