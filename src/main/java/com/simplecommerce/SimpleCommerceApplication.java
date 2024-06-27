package com.simplecommerce;

import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @since 1.0
 * @author julius.krah
 */
@SpringBootApplication(proxyBeanMethods = false)
public class SimpleCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleCommerceApplication.class, args);
    }

    @Bean
    RouterFunction<?> home() {
        return RouterFunctions.route()
                .GET("/", request -> ServerResponse.ok().body("Hello World!"))
                .build();
    }

    @Bean
    RouterFunction<?> index() {
        return RouterFunctions.route()
                .GET("/index", request -> ServerResponse.permanentRedirect(URI.create("/"))
                .build())
                .build();
    }

}
