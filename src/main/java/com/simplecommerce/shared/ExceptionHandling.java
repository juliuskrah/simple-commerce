package com.simplecommerce.shared;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author julius.krah
 */
@ControllerAdvice
public class ExceptionHandling {

  @GraphQlExceptionHandler(NotFoundException.class)
  GraphQLError handleNotFound(DataFetchingEnvironment env) {
    return GraphQLError.newError().message("Cannot be found")
        .errorType(ErrorType.NOT_FOUND)
        .path(env.getExecutionStepInfo().getPath())
        .location(env.getMergedField().getSingleField().getSourceLocation())
        .build();
  }
}