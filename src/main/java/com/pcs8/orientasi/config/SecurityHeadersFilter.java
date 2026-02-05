package com.pcs8.orientasi.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter untuk menambahkan security headers pada setiap response.
 * 
 * Headers yang ditambahkan:
 * - Strict-Transport-Security: Enforce HTTPS
 * - X-Content-Type-Options: Prevent MIME type sniffing
 * - X-Frame-Options: Prevent clickjacking
 * - X-XSS-Protection: Enable XSS protection
 */
@Component
public class SecurityHeadersFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SecurityHeadersFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set security headers
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        log.debug("Security headers added to response");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
