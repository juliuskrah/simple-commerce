package com.simplecommerce.security.aspects;

/**
 * Aspect that intercepts methods annotated with @Check and applies security checks.
 */
public aspect CheckAspect extends AbstractMethodInterceptorAspect {

  protected pointcut executionOfAnnotatedMethod(): execution(* *(..)) && @annotation(Check);
}
