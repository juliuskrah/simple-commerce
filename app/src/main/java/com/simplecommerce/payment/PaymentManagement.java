package com.simplecommerce.payment;

import com.simplecommerce.order.Orders;
import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.utils.MonetaryUtils;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of PaymentService.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
class PaymentManagement implements PaymentService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentManagement.class);

  private Payments paymentRepository;
  private Orders orderRepository;

  public void setPaymentRepository(ObjectFactory<Payments> paymentRepository) {
    this.paymentRepository = paymentRepository.getObject();
  }

  public void setOrderRepository(ObjectFactory<Orders> orderRepository) {
    this.orderRepository = orderRepository.getObject();
  }

  @Override
  public Payment createPayment(CreatePaymentInput input) {
    LOG.debug("Creating payment for order: {}", input.orderId());

    var globalId = GlobalId.decode(input.orderId());
    var order = orderRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Order not found"));

    var payment = new PaymentEntity();
    payment.setOrder(order);
    payment.setPaymentMethod(input.paymentMethod());
    payment.setPaymentProvider(determineProvider(input.paymentMethod()));
    payment.setAmount(new BigDecimal(input.amount()));
    payment.setCurrency(input.currency());
    payment.setStatus(PaymentStatus.PENDING);

    // TODO: Integrate with actual payment provider
    // For now, just mark as authorized
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setAuthorizedAt(OffsetDateTime.now());

    payment = paymentRepository.save(payment);

    LOG.info("Payment created: id={}, orderId={}, amount={}",
        payment.getId(), order.getId(), payment.getAmount());

    return toPayment(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public Payment getPayment(String id) {
    var globalId = GlobalId.decode(id);
    var payment = paymentRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Payment not found"));
    return toPayment(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Payment> getPaymentsByOrder(String orderId) {
    var globalId = GlobalId.decode(orderId);
    return paymentRepository.findByOrderId(UUID.fromString(globalId.id()))
        .stream()
        .map(this::toPayment)
        .toList();
  }

  @Override
  public Payment capturePayment(String id) {
    var globalId = GlobalId.decode(id);
    var payment = paymentRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Payment not found"));

    if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
      throw new IllegalStateException("Payment must be authorized to capture");
    }

    // TODO: Integrate with actual payment provider
    payment.setStatus(PaymentStatus.CAPTURED);
    payment.setCapturedAt(OffsetDateTime.now());

    payment = paymentRepository.save(payment);

    LOG.info("Payment captured: id={}", payment.getId());
    return toPayment(payment);
  }

  @Override
  public Payment refundPayment(String id, String amount, String reason) {
    var globalId = GlobalId.decode(id);
    var payment = paymentRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Payment not found"));

    if (payment.getStatus() != PaymentStatus.CAPTURED) {
      throw new IllegalStateException("Only captured payments can be refunded");
    }

    // TODO: Implement refund logic with provider
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setRefundedAt(OffsetDateTime.now());

    payment = paymentRepository.save(payment);

    LOG.info("Payment refunded: id={}, reason={}", payment.getId(), reason);
    return toPayment(payment);
  }

  private String determineProvider(PaymentMethod method) {
    return switch (method) {
      case STRIPE -> "stripe";
      case PAYPAL -> "paypal";
      case CREDIT_CARD, DEBIT_CARD -> "stripe"; // Default to Stripe for cards
      case BANK_TRANSFER -> "bank";
      case CASH_ON_DELIVERY -> "cash";
      case CRYPTO -> "crypto";
    };
  }

  private Payment toPayment(PaymentEntity entity) {
    var currency = MonetaryUtils.getCurrency(entity.getCurrency(), java.util.Locale.getDefault());
    var money = new com.simplecommerce.shared.types.Money(currency, entity.getAmount());

    return new Payment(
        new GlobalId("Payment", entity.getId().toString()).encode(),
        new GlobalId("Order", entity.getOrder().getId().toString()).encode(),
        entity.getPaymentMethod(),
        entity.getPaymentProvider(),
        entity.getTransactionId(),
        money,
        entity.getStatus(),
        entity.getCardLastFour(),
        entity.getCardBrand(),
        entity.getCreatedAt(),
        entity.getCapturedAt()
    );
  }
}
