package com.simplecommerce.payment;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import com.simplecommerce.order.OrderEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

/**
 * A payment represents a financial transaction for an order.
 *
 * @author julius.krah
 */
@Entity(name = "Payment")
@Table(name = "payments")
public class PaymentEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @Enumerated(STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  private PaymentMethod paymentMethod;

  @Column(name = "payment_provider", nullable = false, length = 50)
  private String paymentProvider;

  @Column(name = "transaction_id")
  @Nullable
  private String transactionId;

  @Column(name = "provider_payment_id")
  @Nullable
  private String providerPaymentId;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Enumerated(STRING)
  @Column(nullable = false, length = 50)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "card_last_four", length = 4)
  @Nullable
  private String cardLastFour;

  @Column(name = "card_brand", length = 50)
  @Nullable
  private String cardBrand;

  @Column(name = "card_exp_month")
  @Nullable
  private Integer cardExpMonth;

  @Column(name = "card_exp_year")
  @Nullable
  private Integer cardExpYear;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Nullable
  private String metadata;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "provider_response", columnDefinition = "jsonb")
  @Nullable
  private String providerResponse;

  @Column(name = "error_message", columnDefinition = "TEXT")
  @Nullable
  private String errorMessage;

  @Column(name = "error_code", length = 100)
  @Nullable
  private String errorCode;

  @Column(name = "risk_level", length = 20)
  @Nullable
  private String riskLevel;

  @Column(name = "fraud_score", precision = 5, scale = 2)
  @Nullable
  private BigDecimal fraudScore;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "authorized_at")
  @Nullable
  private OffsetDateTime authorizedAt;

  @Column(name = "captured_at")
  @Nullable
  private OffsetDateTime capturedAt;

  @Column(name = "failed_at")
  @Nullable
  private OffsetDateTime failedAt;

  @Column(name = "refunded_at")
  @Nullable
  private OffsetDateTime refundedAt;

  public boolean isNew() {
    return id == null;
  }

  // Getters and setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public OrderEntity getOrder() {
    return order;
  }

  public void setOrder(OrderEntity order) {
    this.order = order;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getPaymentProvider() {
    return paymentProvider;
  }

  public void setPaymentProvider(String paymentProvider) {
    this.paymentProvider = paymentProvider;
  }

  @Nullable
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(@Nullable String transactionId) {
    this.transactionId = transactionId;
  }

  @Nullable
  public String getProviderPaymentId() {
    return providerPaymentId;
  }

  public void setProviderPaymentId(@Nullable String providerPaymentId) {
    this.providerPaymentId = providerPaymentId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  @Nullable
  public String getCardLastFour() {
    return cardLastFour;
  }

  public void setCardLastFour(@Nullable String cardLastFour) {
    this.cardLastFour = cardLastFour;
  }

  @Nullable
  public String getCardBrand() {
    return cardBrand;
  }

  public void setCardBrand(@Nullable String cardBrand) {
    this.cardBrand = cardBrand;
  }

  @Nullable
  public Integer getCardExpMonth() {
    return cardExpMonth;
  }

  public void setCardExpMonth(@Nullable Integer cardExpMonth) {
    this.cardExpMonth = cardExpMonth;
  }

  @Nullable
  public Integer getCardExpYear() {
    return cardExpYear;
  }

  public void setCardExpYear(@Nullable Integer cardExpYear) {
    this.cardExpYear = cardExpYear;
  }

  @Nullable
  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(@Nullable String metadata) {
    this.metadata = metadata;
  }

  @Nullable
  public String getProviderResponse() {
    return providerResponse;
  }

  public void setProviderResponse(@Nullable String providerResponse) {
    this.providerResponse = providerResponse;
  }

  @Nullable
  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(@Nullable String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Nullable
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(@Nullable String errorCode) {
    this.errorCode = errorCode;
  }

  @Nullable
  public String getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(@Nullable String riskLevel) {
    this.riskLevel = riskLevel;
  }

  @Nullable
  public BigDecimal getFraudScore() {
    return fraudScore;
  }

  public void setFraudScore(@Nullable BigDecimal fraudScore) {
    this.fraudScore = fraudScore;
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

  @Nullable
  public OffsetDateTime getAuthorizedAt() {
    return authorizedAt;
  }

  public void setAuthorizedAt(@Nullable OffsetDateTime authorizedAt) {
    this.authorizedAt = authorizedAt;
  }

  @Nullable
  public OffsetDateTime getCapturedAt() {
    return capturedAt;
  }

  public void setCapturedAt(@Nullable OffsetDateTime capturedAt) {
    this.capturedAt = capturedAt;
  }

  @Nullable
  public OffsetDateTime getFailedAt() {
    return failedAt;
  }

  public void setFailedAt(@Nullable OffsetDateTime failedAt) {
    this.failedAt = failedAt;
  }

  @Nullable
  public OffsetDateTime getRefundedAt() {
    return refundedAt;
  }

  public void setRefundedAt(@Nullable OffsetDateTime refundedAt) {
    this.refundedAt = refundedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PaymentEntity that)) {
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
    return "PaymentEntity{" +
        "id=" + id +
        ", paymentMethod=" + paymentMethod +
        ", amount=" + amount +
        ", currency='" + currency + '\'' +
        ", status=" + status +
        '}';
  }
}
