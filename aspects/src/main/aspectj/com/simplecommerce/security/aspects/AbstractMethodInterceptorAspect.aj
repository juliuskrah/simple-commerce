package com.simplecommerce.security.aspects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

abstract aspect AbstractMethodInterceptorAspect {

  protected abstract pointcut executionOfAnnotatedMethod();

  private MethodInterceptor securityInterceptor;

  Object around(): executionOfAnnotatedMethod() {
    if (securityInterceptor == null) {
      return proceed();
    }
    MethodInvocation invocation = new JoinPointMethodInvocation(thisJoinPoint, () -> proceed());
    try {
      return securityInterceptor.invoke(invocation);
    } catch (Throwable t) {
      throwUnchecked(t);
      throw new IllegalStateException("Code unexpectedly reached", t);
    }
  }

  public void setSecurityInterceptor(MethodInterceptor securityInterceptor) {
    this.securityInterceptor = securityInterceptor;
  }

  private static void throwUnchecked(Throwable ex) {
    throwAny(ex);
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> void throwAny(Throwable ex) throws E {
    throw (E) ex;
  }
}