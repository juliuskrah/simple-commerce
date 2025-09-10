package com.simplecommerce.config;

import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.ValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;
import org.springframework.context.MessageSource;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Component
class ValidationDirectivesConfigurer implements RuntimeWiringConfigurer {
  final MessageSource messageSource;

  ValidationDirectivesConfigurer(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Register validation directives.
   */
  @Override
  public void configure(Builder builder) {
    ValidationRules validationRules = ValidationRules.newValidationRules()
        .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
        .messageInterpolator(new DefaultResourceBundleMessageInterpolator(messageSource))
        .build();

    ValidationSchemaWiring schemaWiring = new ValidationSchemaWiring(validationRules);
    builder.directiveWiring(schemaWiring);
  }

}
