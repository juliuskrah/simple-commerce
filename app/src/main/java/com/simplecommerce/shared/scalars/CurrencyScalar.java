package com.simplecommerce.shared.scalars;

import static graphql.scalars.util.Kit.typeName;

import com.simplecommerce.shared.utils.MonetaryUtils;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.util.Locale;
import java.util.function.Function;
import javax.money.CurrencyUnit;

/**
 * @author julius.krah
 */
public final class CurrencyScalar {
  public static final GraphQLScalarType INSTANCE;

  private CurrencyScalar() {}

  static {
    var coercing = new Coercing<CurrencyUnit, String>() {
      @Override
      public String serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale) {
        CurrencyUnit currency = parseCurrency(dataFetcherResult, locale, CoercingSerializeException::new);
        return currency.getCurrencyCode();
      }

      @Override
      public CurrencyUnit parseValue(Object input, GraphQLContext graphQLContext, Locale locale) {
        return parseCurrency(input, locale, CoercingParseValueException::new);
      }

      @Override
      public CurrencyUnit parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale) {
        if (!(input instanceof StringValue string)) {
          throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + typeName(input) + "'.");
        }
        String stringValue = string.getValue();
        return parseCurrency(stringValue, locale, CoercingParseLiteralException::new);
      }

      @Override
      public Value<?> valueToLiteral(Object input, GraphQLContext graphQLContext, Locale locale) {
        String serializedInput = serialize(input, graphQLContext, locale);
        return StringValue.newStringValue(serializedInput).build();
      }

      private CurrencyUnit parseCurrency(Object input, Locale locale, Function<String, RuntimeException> exceptionMaker) {
        CurrencyUnit result;
        if (input instanceof CurrencyUnit currency) {
          result = currency;
        } else if (input instanceof String currencyCode) {
          result = MonetaryUtils.getCurrency(currencyCode, locale);
        }else {
          throw exceptionMaker.apply("Expected a 'String' or 'Currency' but was '" + typeName(input) + "'.");
        }
        return result;
      }

    };
    INSTANCE = GraphQLScalarType.newScalar()
        .name("Currency")
        .description("An ISO-4217 compliant Currency Scalar")
        .coercing(coercing).build();
  }
}
