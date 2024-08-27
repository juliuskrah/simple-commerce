package com.simplecommerce.shared.config;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.stereotype.Component;

/**
 * @author julius.krah
 */
@Component
class SecurityTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    return new DelegatingSecurityContextRunnable(runnable);
  }
}
