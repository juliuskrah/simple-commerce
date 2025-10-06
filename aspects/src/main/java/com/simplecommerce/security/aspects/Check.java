package com.simplecommerce.security.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PostAuthorize;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@PostAuthorize("@authz.checkPermission('{namespace}', {object}, '{relation}', {subject}, {returnObject})")
public @interface Check {

  String namespace();

  String object() default "#id";

  String relation();

  String subject() default "authentication.name";

  String returnObject() default "returnObject";
}
