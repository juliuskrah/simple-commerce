package com.simplecommerce.shared;

import com.simplecommerce.shared.exceptions.NotFoundException;
import com.simplecommerce.shared.exceptions.OperationNotAllowedException;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
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

  @GraphQlExceptionHandler
  GraphQLError handleOperationNotAllowed(OperationNotAllowedException ex, DataFetchingEnvironment env) {
    return GraphQLError.newError().message(ex.getMessage())
        .errorType(ErrorType.BAD_REQUEST)
        .path(env.getExecutionStepInfo().getPath())
        .location(env.getMergedField().getSingleField().getSourceLocation())
        .build();
  }

  @GraphQlExceptionHandler(DataIntegrityViolationException.class)
  GraphQLError handleDuplicate(DataFetchingEnvironment env) {
    return GraphQLError.newError().message("Looks like a duplicate")
        .errorType(ErrorClassification.errorClassification("DUPLICATE"))
        .path(env.getExecutionStepInfo().getPath())
        .location(env.getMergedField().getSingleField().getSourceLocation())
        .build();
  }

  @GraphQlExceptionHandler
  GraphQLError handleValidationError(ConstraintViolationException ex, DataFetchingEnvironment env) {
    Map<String, Object> extensions = new HashMap<>();
    ex.getConstraintViolations().forEach(constraintViolation ->
        extensions.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage())
    );
    return GraphQLError.newError().message("Some fields have invalid values")
        .errorType(ErrorType.BAD_REQUEST)
        .path(env.getExecutionStepInfo().getPath())
        .extensions(extensions)
        .location(env.getMergedField().getSingleField().getSourceLocation())
        .build();
  }
}
