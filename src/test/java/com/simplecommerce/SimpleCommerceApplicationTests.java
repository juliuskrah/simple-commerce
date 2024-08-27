package com.simplecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author julius.krah
 */
class SimpleCommerceApplicationTests {

	@Test
	void verifyModuleStructure() {
		var modules = ApplicationModules.of(SimpleCommerceApplication.class);
		modules.forEach(System.out::println);
		modules.verify();
	}
}
