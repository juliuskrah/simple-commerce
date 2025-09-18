package com.simplecommerce.shared.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

/**
 * @author julius.krah
 */
public final class VirtualThreadHelper {

  private static final ReentrantLock lock = new ReentrantLock();

  private VirtualThreadHelper() {
    throw new IllegalStateException("Utility class");
  }

  public static void runInScope(Runnable run) {
    lock.lock();
    try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow(), Function.identity())) {
      DelegatingSecurityContextRunnable delegate = new DelegatingSecurityContextRunnable(run);
      scope.fork(delegate);
      scope.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new com.simplecommerce.shared.exceptions.CommerceException(e);
    } catch (Exception e) {
      throw new com.simplecommerce.shared.exceptions.CommerceException(e);
    } finally {
      lock.unlock();
    }
  }

  public static <R> R callInScope(Callable<R> call) {
    lock.lock();
    try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow(), Function.identity())) {
      DelegatingSecurityContextCallable<R> delegate = new DelegatingSecurityContextCallable<>(call);
      var task = scope.fork(delegate);
      scope.join();
      return task.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new com.simplecommerce.shared.exceptions.CommerceException(e);
    } catch (Exception e) {
      throw new com.simplecommerce.shared.exceptions.CommerceException(e);
    } finally {
      lock.unlock();
    }
  }
}
