package com.simplecommerce.security.aspects;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.util.Assert;

class JoinPointMethodInvocation implements MethodInvocation {

  private final JoinPoint jp;

  private final Method method;

  private final Object target;

  private final Supplier<Object> proceed;

  JoinPointMethodInvocation(JoinPoint jp, Supplier<Object> proceed) {
    this.jp = jp;
    if (jp.getTarget() != null) {
      this.target = jp.getTarget();
    } else {
      // SEC-1295: target may be null if an ITD is in use
      this.target = jp.getSignature().getDeclaringType();
    }
    String targetMethodName = jp.getStaticPart().getSignature().getName();
    Class<?>[] types = ((CodeSignature) jp.getStaticPart().getSignature()).getParameterTypes();
    Class<?> declaringType = jp.getStaticPart().getSignature().getDeclaringType();
    this.method = findMethod(targetMethodName, declaringType, types);
    Assert.notNull(method, () -> "Could not obtain target method from JoinPoint: '" + jp + "'");
    this.proceed = proceed;
  }

  private Method findMethod(String name, Class<?> declaringType, Class<?>[] params) {
    Method method = null;
    try {
      method = declaringType.getMethod(name, params);
    } catch (NoSuchMethodException _) {
      // Swallow - try getDeclaredMethod next
    }
    if (method == null) {
      try {
        method = declaringType.getDeclaredMethod(name, params);
      } catch (NoSuchMethodException _) {
        // Swallow - return null
      }
    }
    return method;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public Object[] getArguments() {
    return jp.getArgs();
  }

  @Override
  public AccessibleObject getStaticPart() {
    return method;
  }

  @Override
  public Object getThis() {
    return target;
  }

  @Override
  public Object proceed() {
    return proceed.get();
  }
}
