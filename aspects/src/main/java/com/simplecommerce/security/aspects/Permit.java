package com.simplecommerce.security.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@PreAuthorize("@authz.checkPermission('{namespace}', {object}, '{relation}', {subject})")
public @interface Permit {
  String namespace();
  String object() default "#id";
  String relation();
  String subject() default "authentication.name";
}
