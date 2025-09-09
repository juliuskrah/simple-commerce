package com.simplecommerce.shared.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation requiring STAFF role.
 * Only Staff actors (admins, vendors) can access annotated methods.
 * 
 * @author julius.krah
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('STAFF')")
public @interface RequireStaffRole {
}