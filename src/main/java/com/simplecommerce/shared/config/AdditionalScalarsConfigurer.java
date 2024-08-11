package com.simplecommerce.shared.config;

import static graphql.scalars.ExtendedScalars.Currency;
import static graphql.scalars.ExtendedScalars.DateTime;
import static graphql.scalars.ExtendedScalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.Url;

import graphql.scalars.ExtendedScalars;
import graphql.schema.idl.RuntimeWiring.Builder;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;

/**
 * Configuration for GraphQL.
 *
 * @author julius.krah
 * @since 1.0
 */
@Component
class AdditionalScalarsConfigurer implements RuntimeWiringConfigurer {

  /**
   * Register custom scalars.
   */
  @Override
  public void configure(Builder builder) {
    builder.scalar(DateTime)
        .scalar(Currency)
        .scalar(ExtendedScalars.newAliasedScalar("Decimal")
            .aliasedScalar(GraphQLBigDecimal)
            .build())
        .scalar(ExtendedScalars.newAliasedScalar("JsonString")
            .aliasedScalar(GraphQLString)
            .build())
        .scalar(ExtendedScalars.newAliasedScalar("URL")
            .aliasedScalar(Url)
            .build());
  }
}
