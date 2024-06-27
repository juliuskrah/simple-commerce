package com.simplecommerce;

import static graphql.scalars.ExtendedScalars.Currency;
import static graphql.scalars.ExtendedScalars.DateTime;
import static graphql.scalars.ExtendedScalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.Url;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration for GraphQL.
 *
 * @author julius.krah
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
class GraphQLConfiguration {

  /**
   * Register custom scalars.
   *
   * @return RuntimeWiringConfigurer
   */
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder.scalar(DateTime)
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
