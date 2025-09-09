package com.simplecommerce.shared.authorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method-level authorization checks using Keto.
 * Can be used on controller methods to enforce ReBAC permissions.
 * 
 * @author julius.krah
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPermission {
    
    /**
     * The namespace to check permissions against (e.g., "Customer", "Staff", "Bot").
     */
    String namespace();
    
    /**
     * The relation to check (e.g., "read", "write", "delete").
     */
    String relation();
    
    /**
     * SpEL expression to extract the object identifier from method parameters.
     * Default is "#id" which looks for a parameter named "id".
     */
    String object() default "#id";
    
    /**
     * SpEL expression to extract the subject identifier.
     * Default extracts from Spring Security context.
     */
    String subject() default "authentication.name";
}