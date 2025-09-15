package com.simplecommerce.shared.authorization;

import org.springframework.security.access.prepost.PostFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation for filtering collections by ownership.
 * Filters returned collections to only include items owned by the current user.
 * 
 * @author julius.krah
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PostFilter("@ketoAuthorizationService.checkPermission('', authentication.name, 'read', filterObject.id)")
public @interface FilterByOwnership {
}