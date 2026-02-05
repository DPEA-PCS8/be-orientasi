package com.pcs8.orientasi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filter untuk validasi headers pada setiap incoming request.
 * 
 * Validasi yang dilakukan:
 * 1. APIKey header harus ada dan sesuai dengan konfigurasi
 * 2. Content-Type harus application/json untuk POST/PUT/PATCH
 * 3. Authorization header (Bearer token) diperlukan untuk semua endpoint kecuali public endpoints
 * 
 * Public endpoints yang tidak memerlukan Bearer token:
 * - /api/auth/login
 * - /api/crypto/encrypt
 * - /api/crypto/decrypt
 */
@Component
public class AuthHeaderFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthHeaderFilter.class);

    // Endpoints yang tidak butuh Bearer token
    private static final Set<String> PUBLIC_ENDPOINTS = new HashSet<>(Arrays.asList(
            "/auth/login",
            "/crypto/encrypt",
            "/crypto/decrypt",
            "/api/auth/login",
            "/api/crypto/encrypt",
            "/api/crypto/decrypt"
    ));

    @Value("${app.api-key:da39b92f-a1b8-46d5-a10c-d08b1cc92218}")
    private String configuredApiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        log.debug("Request: {} {}", method, requestPath);

        try {
            // Validate required headers (APIKey dan Content-Type)
            if (!isValidRequiredHeaders(httpRequest)) {
                sendErrorResponse(httpResponse, 400, "Missing or invalid required headers");
                return;
            }

            // Check if endpoint requires Bearer token
            if (!isPublicEndpoint(requestPath)) {
                String authHeader = httpRequest.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Missing or invalid Authorization header for: {}", requestPath);
                    sendErrorResponse(httpResponse, 401, "Missing or invalid Authorization header");
                    return;
                }
            }

            // All validation passed, continue to next filter
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in AuthHeaderFilter: {}", e.getMessage());
            sendErrorResponse(httpResponse, 500, "Internal server error");
        }
    }

    /**
     * Validasi headers yang diperlukan:
     * - APIKey: harus sesuai dengan konfigurasi
     * - Content-Type: harus application/json untuk method POST/PUT/PATCH
     */
    private boolean isValidRequiredHeaders(HttpServletRequest request) {
        // Validate APIKey
        String apiKey = request.getHeader("APIKey");
        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            log.warn("Invalid or missing APIKey header");
            return false;
        }

        // Validate Content-Type (for POST, PUT, PATCH requests)
        String method = request.getMethod();
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
            String contentType = request.getHeader("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                log.warn("Invalid or missing Content-Type header for {} request", method);
                return false;
            }
        }

        return true;
    }

    /**
     * Check apakah endpoint adalah public endpoint (tidak memerlukan Bearer token)
     */
    private boolean isPublicEndpoint(String path) {
        if (path == null) {
            return false;
        }
        return PUBLIC_ENDPOINTS.contains(path);
    }

    /**
     * Send error response dalam format BaseResponse
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseResponse errorResponse = new BaseResponse(status, message, null);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
        
        log.warn("Response status: {} - {}", status, message);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
