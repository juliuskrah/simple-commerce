package com.simplecommerce;

import static java.lang.ScopedValue.where;

import java.lang.ScopedValue.Carrier;

/// A holder for the `AuthenticationContext` using Java's ScopedValue feature.
/// @author julius.krah
public final class AuthenticationContextHolder {

  private static final ScopedValue<AuthenticationContext> ACCESS_TOKEN_SCOPE = ScopedValue.newInstance();

  public static AuthenticationContext getContext() {
    if (ACCESS_TOKEN_SCOPE.isBound()) {
      return ACCESS_TOKEN_SCOPE.get();
    }
    return new AuthenticationContext(null, 0, null, null, null, null);
  }

  public static Carrier setContext(AuthenticationContext context) {
    return where(ACCESS_TOKEN_SCOPE, context);
  }
}
