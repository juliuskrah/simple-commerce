package com.simplecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.graphql.data.pagination.CursorEncoder;
import org.springframework.graphql.data.pagination.CursorStrategy;
import org.springframework.graphql.data.pagination.EncodingCursorStrategy;
import org.springframework.graphql.data.query.JsonKeysetCursorStrategy;
import org.springframework.graphql.data.query.ScrollPositionCursorStrategy;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
class GraphQlConfiguration {

  @Bean
  EncodingCursorStrategy<ScrollPosition> cursorStrategy() {
    return CursorStrategy.withEncoder(new ScrollPositionCursorStrategy(
        new JsonKeysetCursorStrategy(configurer())
    ), CursorEncoder.base64());
  }

  private ServerCodecConfigurer configurer() {
    ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
    customizeJsonCodec(configurer);
    return configurer;
  }

  /*
   * Customize the JSON codec to support UUID polymorphic types.
   */
  private void customizeJsonCodec(CodecConfigurer configurer) {
    PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Map.class)
        .allowIfSubType("java.time.")
        .allowIfSubType(UUID.class)
        .build();

    ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
    mapper.activateDefaultTyping(validator, DefaultTyping.NON_FINAL);

    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
  }
}
