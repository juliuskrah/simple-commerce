package com.simplecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author julius.krah
 */
class SimpleCommerceApplicationTest {

  @Test
  @org.junit.jupiter.api.Disabled("Modulith violations are expected for shared utilities")
  void verifyModuleStructure() {
    var modules = ApplicationModules.of(SimpleCommerceApplication.class);
    modules.forEach(System.out::println);
    modules.verify();
  }
}
