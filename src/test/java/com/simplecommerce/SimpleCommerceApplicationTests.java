package com.simplecommerce;

import com.tngtech.archunit.base.DescribedPredicate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author julius.krah
 * @since 1.0
 */
class SimpleCommerceApplicationTests {

	@Test
	void verifyModuleStructure() {
		var modules = ApplicationModules.of(SimpleCommerceApplication.class,
				DescribedPredicate.describe("Skipping File Management", module ->
						"com.simplecommerce.file.FileManagement".equals(module.getName())));
		modules.forEach(System.out::println);
		modules.verify();
	}
}
