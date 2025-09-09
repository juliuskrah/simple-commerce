package com.simplecommerce.shared;

import com.simplecommerce.shared.utils.VirtualThreadHelper;
import org.springframework.stereotype.Service;
import java.util.concurrent.Callable;

/**
 * Service for handling async execution with security context.
 * 
 * @author julius.krah
 */
@Service
public class AsyncExecutionService {

  /**
   * Runs a task in scope with security context.
   */
  public void runInScope(Runnable run) {
    VirtualThreadHelper.runInScope(run);
  }

  /**
   * Calls a task in scope with security context and returns result.
   */
  public <R> R callInScope(Callable<R> call) {
    return VirtualThreadHelper.callInScope(call);
  }
}