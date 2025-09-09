package com.simplecommerce.shared.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation for Actor role-based authorization.
 * Combines Spring Security @PreAuthorize with Actor system roles.
 * 
 * @author julius.krah
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'BOT')")
public @interface RequireActorRole {
    
    /**
     * The required actor role (CUSTOMER, STAFF, BOT).
     */
    ActorRole value();
    
    /**
     * Actor role enumeration.
     */
    enum ActorRole {
        CUSTOMER("ROLE_CUSTOMER"),
        STAFF("ROLE_STAFF"), 
        BOT("ROLE_BOT");
        
        private final String springRole;
        
        ActorRole(String springRole) {
            this.springRole = springRole;
        }
        
        public String getSpringRole() {
            return springRole;
        }
    }
}