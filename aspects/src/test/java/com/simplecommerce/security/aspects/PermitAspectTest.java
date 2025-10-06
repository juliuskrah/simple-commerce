package com.simplecommerce.security.aspects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PermitAspectTest {

  @Test
  void testInstanceCreated() {
    var aspect = PermitAspect.aspectOf();
    assertNotNull(aspect);
  }
}
