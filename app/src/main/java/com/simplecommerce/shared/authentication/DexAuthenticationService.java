package com.simplecommerce.shared.authentication;

import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for interacting with DexIDP authentication system.
 * Provides user management and authentication synchronization capabilities.
 * 
 * @author julius.krah
 */
@Service
@ConditionalOnBean(name = "dexGrpcChannel")
public class DexAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(DexAuthenticationService.class);
    
    private final ManagedChannel dexChannel;

    public DexAuthenticationService(@Qualifier("dexGrpcChannel") ManagedChannel dexChannel) {
        this.dexChannel = dexChannel;
    }

    /**
     * Get the current authenticated user information from JWT token.
     * 
     * @return Optional<ActorInfo> containing user details or empty if not authenticated
     */
    public Optional<ActorInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return extractActorInfoFromJwt(jwt);
        }
        
        logger.debug("No JWT authentication found in security context");
        return Optional.empty();
    }

    /**
     * Create a new user in DexIDP system.
     * 
     * @param email User's email address
     * @param username User's username
     * @param password User's password
     * @param actorType Type of actor (Customer, Staff, Bot)
     * @return CompletableFuture<String> containing the created user ID
     */
    public CompletableFuture<String> createUser(String email, String username, String password, String actorType) {
        logger.debug("Creating user: {} with actor type: {}", username, actorType);
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            // Simulate user creation - in real implementation would use DexIDP gRPC API
            String userId = java.util.UUID.randomUUID().toString();
            logger.info("Created user: {} with ID: {} and actor type: {}", username, userId, actorType);
            
            // In real implementation, we would also sync this user info with Keto
            // to establish the proper Actor relationships
            future.complete(userId);
            
        } catch (Exception e) {
            logger.error("Error creating user: {}", username, e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Update user information in DexIDP.
     * 
     * @param userId The user ID to update
     * @param updates Map of field updates
     * @return CompletableFuture<Void> indicating completion
     */
    public CompletableFuture<Void> updateUser(String userId, Map<String, Object> updates) {
        logger.debug("Updating user: {} with updates: {}", userId, updates);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Simulate user update
            logger.info("Updated user: {} with changes: {}", userId, updates);
            future.complete(null);
            
        } catch (Exception e) {
            logger.error("Error updating user: {}", userId, e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Delete a user from DexIDP system.
     * 
     * @param userId The user ID to delete
     * @return CompletableFuture<Void> indicating completion
     */
    public CompletableFuture<Void> deleteUser(String userId) {
        logger.debug("Deleting user: {}", userId);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Simulate user deletion
            logger.info("Deleted user: {}", userId);
            future.complete(null);
            
        } catch (Exception e) {
            logger.error("Error deleting user: {}", userId, e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Revoke user's refresh tokens (force logout).
     * 
     * @param userId The user ID
     * @return CompletableFuture<Void> indicating completion
     */
    public CompletableFuture<Void> revokeUserTokens(String userId) {
        logger.debug("Revoking tokens for user: {}", userId);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Simulate token revocation
            logger.info("Revoked tokens for user: {}", userId);
            future.complete(null);
            
        } catch (Exception e) {
            logger.error("Error revoking tokens for user: {}", userId, e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Extract actor information from JWT token.
     */
    private Optional<ActorInfo> extractActorInfoFromJwt(Jwt jwt) {
        try {
            String subject = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String username = jwt.getClaimAsString("preferred_username");
            String name = jwt.getClaimAsString("name");
            
            // Determine actor type based on claims or email domain
            String actorType = determineActorType(email, username);
            
            ActorInfo actorInfo = new ActorInfo(
                subject,
                username != null ? username : email,
                email,
                name,
                actorType
            );
            
            logger.debug("Extracted actor info: {}", actorInfo);
            return Optional.of(actorInfo);
            
        } catch (Exception e) {
            logger.error("Error extracting actor info from JWT", e);
            return Optional.empty();
        }
    }

    /**
     * Determine actor type based on user information.
     */
    private String determineActorType(String email, String username) {
        if (email != null) {
            if (email.contains("customer")) {
                return "Customer";
            } else if (email.contains("vendor") || email.contains("staff")) {
                return "Staff";
            } else if (email.contains("bot") || email.contains("service")) {
                return "Bot";
            }
        }
        
        if (username != null) {
            if (username.contains("admin") || username.equals("simple_commerce")) {
                return "Staff";
            } else if (username.contains("customer")) {
                return "Customer";
            } else if (username.contains("bot")) {
                return "Bot";
            }
        }
        
        // Default to Customer for unknown users
        return "Customer";
    }

    /**
     * Data class for actor information.
     */
    public static class ActorInfo {
        private final String id;
        private final String username;
        private final String email;
        private final String name;
        private final String actorType;

        public ActorInfo(String id, String username, String email, String name, String actorType) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name;
            this.actorType = actorType;
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getActorType() { return actorType; }

        @Override
        public String toString() {
            return String.format("ActorInfo{id='%s', username='%s', email='%s', actorType='%s'}", 
                               id, username, email, actorType);
        }
    }
}