package com.pcs8.orientasi.config;

import com.pcs8.orientasi.config.annotation.PublicAccess;
import com.pcs8.orientasi.config.annotation.RequiresRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

/**
 * Interceptor to enforce role-based authorization using custom annotations.
 * This interceptor checks @RequiresRole annotations on controller methods.
 */
@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    private final JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check for @PublicAccess annotation (takes precedence)
        if (handlerMethod.hasMethodAnnotation(PublicAccess.class) ||
                handlerMethod.getBeanType().isAnnotationPresent(PublicAccess.class)) {
            log.debug("Public access allowed for: {}", request.getRequestURI());
            return true;
        }

        // Get JWT token from request
        String token = extractToken(request);
        if (token == null) {
            log.warn("No JWT token found in request");
            sendUnauthorizedResponse(response, "Authentication required");
            return false;
        }

        // Parse token to get user claims
        Claims claims;
        try {
            claims = jwtConfig.parseToken(token);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Invalid or expired token");
            return false;
        }

        // Extract roles from JWT
        Set<String> userRoles = extractSetFromClaims(claims, "roles");
        Boolean hasRole = claims.get("has_role", Boolean.class);

        // Store user info in request attributes for later use
        request.setAttribute("user_uuid", claims.get("uuid", String.class));
        request.setAttribute("user_roles", userRoles);
        request.setAttribute("has_role", hasRole);

        // Check @RequiresRole annotation
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole == null) {
            requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }

        if (requiresRole != null) {
            if (!hasRole || userRoles.isEmpty()) {
                log.warn("User has no role assigned. Access denied to: {}", request.getRequestURI());
                sendForbiddenResponse(response, "You have not been assigned a role. Please contact an administrator.");
                return false;
            }

            String[] requiredRoles = requiresRole.value();
            boolean requireAll = requiresRole.requireAll();

            if (!checkRoles(userRoles, requiredRoles, requireAll)) {
                log.warn("User does not have required roles. Access denied to: {}", request.getRequestURI());
                sendForbiddenResponse(response, "Insufficient permissions. Required role(s): " + Arrays.toString(requiredRoles));
                return false;
            }
        }

        log.debug("Authorization check passed for: {}", request.getRequestURI());
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractSetFromClaims(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return new HashSet<>();
        }
        if (value instanceof List) {
            return new HashSet<>((List<String>) value);
        }
        if (value instanceof Set) {
            return (Set<String>) value;
        }
        return new HashSet<>();
    }

    private boolean checkRoles(Set<String> userRoles, String[] requiredRoles, boolean requireAll) {
        if (requireAll) {
            // User must have ALL required roles
            return userRoles.containsAll(Arrays.asList(requiredRoles));
        } else {
            // User must have at least ONE of the required roles
            for (String requiredRole : requiredRoles) {
                if (userRoles.contains(requiredRole)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"status\": 401, \"message\": \"%s\", \"data\": null}", message);
        response.getWriter().write(jsonResponse);
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"status\": 403, \"message\": \"%s\", \"data\": null}", message);
        response.getWriter().write(jsonResponse);
    }
}
