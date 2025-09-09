package com.simplecommerce.shared.authorization;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.CompletableFuture;

/**
 * Aspect that intercepts methods annotated with @CheckPermission
 * and performs authorization checks using the Keto service.
 * 
 * @author julius.krah
 */
@Aspect
@Component
@ConditionalOnBean(KetoAuthorizationService.class)
public class PermissionCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckAspect.class);
    
    private final KetoAuthorizationService ketoService;
    private final ExpressionParser parser = new SpelExpressionParser();

    public PermissionCheckAspect(KetoAuthorizationService ketoService) {
        this.ketoService = ketoService;
    }

    @Around("@annotation(checkPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, CheckPermission checkPermission) throws Throwable {
        logger.debug("Checking permission for method: {}", joinPoint.getSignature().getName());
        
        try {
            // Create evaluation context with method parameters
            EvaluationContext context = createEvaluationContext(joinPoint);
            
            // Extract values using SpEL expressions
            String namespace = checkPermission.namespace();
            String relation = checkPermission.relation();
            String objectId = evaluateExpression(checkPermission.object(), context, String.class);
            String subjectId = evaluateExpression(checkPermission.subject(), context, String.class);
            
            logger.debug("Permission check: {}:{}#{} for subject {}", namespace, objectId, relation, subjectId);
            
            // Perform permission check
            CompletableFuture<Boolean> permissionFuture = ketoService.checkPermission(namespace, objectId, relation, subjectId);
            boolean hasPermission = permissionFuture.get(); // Blocking call for simplicity
            
            if (!hasPermission) {
                logger.warn("Access denied for subject {} on {}:{}#{}", subjectId, namespace, objectId, relation);
                throw new AccessDeniedException("Access denied: insufficient permissions");
            }
            
            logger.debug("Permission granted for subject {} on {}:{}#{}", subjectId, namespace, objectId, relation);
            
            // Proceed with method execution
            return joinPoint.proceed();
            
        } catch (AccessDeniedException e) {
            throw e; // Re-throw access denied exceptions
        } catch (Exception e) {
            logger.error("Error during permission check", e);
            throw new AccessDeniedException("Authorization check failed", e);
        }
    }

    /**
     * Creates an evaluation context with method parameters and security context.
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Add method parameters to context
        Object[] args = joinPoint.getArgs();
        Method method = getMethod(joinPoint);
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }
        
        // Add security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            context.setVariable("authentication", authentication);
            context.setVariable("principal", authentication.getPrincipal());
        }
        
        return context;
    }

    /**
     * Evaluates a SpEL expression in the given context.
     */
    private <T> T evaluateExpression(String expressionString, EvaluationContext context, Class<T> type) {
        try {
            Expression expression = parser.parseExpression(expressionString);
            return expression.getValue(context, type);
        } catch (Exception e) {
            logger.error("Failed to evaluate expression: {}", expressionString, e);
            throw new IllegalArgumentException("Invalid SpEL expression: " + expressionString, e);
        }
    }

    /**
     * Gets the method from the join point.
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();
            Class<?>[] parameterTypes = new Class<?>[joinPoint.getArgs().length];
            
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                parameterTypes[i] = joinPoint.getArgs()[i] != null ? 
                    joinPoint.getArgs()[i].getClass() : Object.class;
            }
            
            return targetClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find method", e);
        }
    }
}