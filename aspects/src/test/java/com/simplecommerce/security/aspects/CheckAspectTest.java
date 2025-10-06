package com.simplecommerce.security.aspects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CheckAspectTest {

  @Test
  void testInstanceCreated() {
    var aspect = CheckAspect.aspectOf();
    assertNotNull(aspect);
  }
}
