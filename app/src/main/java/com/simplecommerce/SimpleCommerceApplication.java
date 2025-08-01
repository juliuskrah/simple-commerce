package com.simplecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @since 1.0
 * @author julius.krah
 */
@SpringBootApplication(proxyBeanMethods = false)
public class SimpleCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleCommerceApplication.class, args);
    }

}
