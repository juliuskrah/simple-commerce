package com.simplecommerce;

import com.simplecommerce.shared.config.TestDatabaseConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * @author julius.krah
 */
@Inherited
@DataJpaTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestDatabaseConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public @interface DataPostgresTest {

  @AliasFor(annotation = DataJpaTest.class)
  String[] properties() default {};
}
