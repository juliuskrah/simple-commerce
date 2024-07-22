package com.simplecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author julius.krah
 * @since 1.0
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
class SimpleCommerceApplicationTests {

	@Test
	void verifyModuleStructure() {
		var modules = ApplicationModules.of(SimpleCommerceApplication.class);
		modules.forEach(System.out::println);
		modules.verify();
	}
}
