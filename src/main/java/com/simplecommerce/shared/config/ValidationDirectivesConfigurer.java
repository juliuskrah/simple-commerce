package com.simplecommerce.shared.config;

import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.ValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Component
class ValidationDirectivesConfigurer implements RuntimeWiringConfigurer {

  /**
   * Register validation directives.
   */
  @Override
  public void configure(Builder builder) {
    ValidationRules validationRules = ValidationRules.newValidationRules()
        .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
        .build();

    ValidationSchemaWiring schemaWiring = new ValidationSchemaWiring(validationRules);
    builder.directiveWiring(schemaWiring);
  }

}
