package com.simplecommerce.shipping;

import com.simplecommerce.shared.GlobalId;
import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.utils.MonetaryUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ShippingService.
 *
 * @author julius.krah
 */
@Service
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class ShippingManagement implements ShippingService {

  private static final Logger LOG = LoggerFactory.getLogger(ShippingManagement.class);

  private ShippingMethods shippingMethodRepository;

  public void setShippingMethodRepository(ObjectFactory<ShippingMethods> shippingMethodRepository) {
    this.shippingMethodRepository = shippingMethodRepository.getObject();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShippingMethod> getAvailableShippingMethods(
      String country,
      String state,
      BigDecimal orderAmount) {

    LOG.debug("Finding shipping methods for: country={}, state={}, amount={}",
        country, state, orderAmount);

    var methods = shippingMethodRepository.findByCountryAndState(country, state);

    return methods.stream()
        .filter(m -> m.isApplicableForAmount(orderAmount))
        .map(this::toShippingMethod)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ShippingMethod getShippingMethod(String id) {
    var globalId = GlobalId.decode(id);
    var method = shippingMethodRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Shipping method not found"));
    return toShippingMethod(method);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateShippingCost(String methodId, BigDecimal orderAmount) {
    var globalId = GlobalId.decode(methodId);
    var method = shippingMethodRepository.findById(UUID.fromString(globalId.id()))
        .orElseThrow(() -> new NotFoundException("Shipping method not found"));

    // Check if free shipping threshold is met
    if (method.getMinOrderAmount() != null &&
        orderAmount.compareTo(method.getMinOrderAmount()) >= 0 &&
        method.getPriceAmount().compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return method.getPriceAmount();
  }

  private ShippingMethod toShippingMethod(ShippingMethodEntity entity) {
    var currency = MonetaryUtils.getCurrency(entity.getPriceCurrency(), Locale.getDefault());
    var money = new com.simplecommerce.shared.types.Money(currency, entity.getPriceAmount());

    return new ShippingMethod(
        new GlobalId("ShippingMethod", entity.getId().toString()).encode(),
        entity.getName(),
        entity.getDescription(),
        entity.getCode(),
        entity.getCarrier(),
        money,
        entity.getMinDeliveryDays(),
        entity.getMaxDeliveryDays()
    );
  }
}
