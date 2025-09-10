package com.simplecommerce.config;

import java.net.URI;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.RouterFunctions.Builder;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration(proxyBeanMethods = false)
class GraphQlEntryPointConfigurer implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/svelte/");
  }

  @Bean
  RouterFunction<ServerResponse> graphiql(GraphQlProperties graphQlProperties) {
    Builder builder = RouterFunctions.route();
    // ClassPathResource graphiQlPage = new ClassPathResource("/ui/graphiql/index.html");
    //    GraphiQlHandler graphiQLHandler = new GraphiQlHandler(
    //        graphQlProperties.getHttp().getPath(),
    //        graphQlProperties.getWebsocket().getPath(),
    //        graphiQlPage
    //    );
    //  builder = builder.GET("/", graphiQLHandler::handleRequest);
    builder.GET(graphQlProperties.getGraphiql().getPath(), request -> ServerResponse.ok().body("Hello World!"));
    return builder.build();
  }

  @Bean
  RouterFunction<ServerResponse> home() {
    Builder builder = RouterFunctions.route();
    ClassPathResource svelte = new ClassPathResource("/svelte/index.html");
    builder.GET("/", request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(svelte));
    return builder.build();
  }

  @Bean
  RouterFunction<ServerResponse> index() {
    return RouterFunctions.route()
        .GET("/index.html", request -> ServerResponse.temporaryRedirect(URI.create("/"))
            .build())
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> graphQlPreflight() {
    return RouterFunctions.route()
        .OPTIONS("/graphql", request -> ServerResponse.ok().build())
        .build();
  }
}
