package com.simplecommerce;

import static java.lang.ScopedValue.where;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/// A holder for the [AuthenticationContext] using Java's ScopedValue feature.
/// @author julius.krah
public final class AuthenticationContextHolder {

  private static final ScopedValue<AuthenticationContext> ACCESS_TOKEN_SCOPE = ScopedValue.newInstance();

  /// Current thread's [AuthenticationContext].
  /// If no context is bound to the current thread, `null` is returned.
  /// @return The current thread's `AuthenticationContext`.
  /// @see #withContext(AuthenticationContext, Supplier)
  @Nullable
  public static AuthenticationContext getContext() {
    if (ACCESS_TOKEN_SCOPE.isBound()) {
      return ACCESS_TOKEN_SCOPE.get();
    }
    return null;
  }
  /// Executes the given supplier within the context of the provided [AuthenticationContext].
  /// This method binds the `AuthenticationContext` to the current thread for the duration of the
  /// supplier's execution.
  ///
  /// ```java
  /// class AuthenticationUtils {
  ///     public static String getAccessToken() {
  ///       var context = AuthenticationContextHolder.getContext(); // Retrieve the current context
  ///       // Use context to perform authentication and obtain access token
  ///       ...
  ///     }
  /// }
  ///
  /// void someMethod() {
  ///     AuthenticationContext context = ...; // Create or obtain an AuthenticationContext
  ///     var result = AuthenticationContextHolder.withContext(context, AuthenticationUtils::getAccessToken);
  ///    // Use the result which is obtained within the bound context
  /// }
  /// ```
  /// @param context The `AuthenticationContext` to bind.
  /// @param supplier The supplier to execute within the bound context.
  /// @param <T> The type of the result produced by the supplier.
  /// @return The result produced by the supplier.
  public static <T> T withContext(AuthenticationContext context, Supplier<T> supplier) {
    return where(ACCESS_TOKEN_SCOPE, context).call(supplier::get);
  }
}
