package com.simplecommerce.shared;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

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
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      DelegatingSecurityContextExecutorService delegate = new DelegatingSecurityContextExecutorService(executor);
      var task = delegate.submit(run);
      task.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CommerceException(e);
    } catch (Exception e) {
      throw new CommerceException(e);
    } finally {
      lock.unlock();
    }
  }

  public static <R> R callInScope(Callable<R> call) {
    lock.lock();
    try (var scope = new ShutdownOnFailure()) {
      DelegatingSecurityContextCallable<R> delegate = new DelegatingSecurityContextCallable<>(call);
      var task = scope.fork(delegate);
      scope.join().throwIfFailed();
      return task.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CommerceException(e);
    } catch (Exception e) {
      throw new CommerceException(e);
    } finally {
      lock.unlock();
    }
  }
}
