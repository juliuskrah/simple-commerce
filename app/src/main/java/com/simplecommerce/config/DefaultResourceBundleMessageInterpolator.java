package com.simplecommerce.config;

import graphql.validation.interpolation.ResourceBundleMessageInterpolator;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;

/**
 * @author julius.krah
 */
class DefaultResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {
  private final MessageSource messageSource;

  DefaultResourceBundleMessageInterpolator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  protected ResourceBundle getResourceBundle(Locale locale) {
    return new MessageSourceResourceBundle(messageSource, locale);
  }
}
