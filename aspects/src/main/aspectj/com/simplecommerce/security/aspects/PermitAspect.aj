package com.simplecommerce.security.aspects;

/**
 * Aspect that intercepts methods annotated with @Permit and applies security checks.
 */
public aspect PermitAspect extends AbstractMethodInterceptorAspect {

  protected pointcut executionOfAnnotatedMethod(): execution(* *(..)) && @annotation(Permit);
}
