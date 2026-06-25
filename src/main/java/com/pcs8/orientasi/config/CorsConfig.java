// filepath: /Users/marvelkrent/Developer/projects/orientasi/workspace/be-orientasi/src/main/java/com/pcs8/orientasi/config/CorsConfig.java
package com.pcs8.orientasi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;

    /** Explicit allowed origins (comma-separated). No wildcard — set FE origins per environment. */
    @Value("${cors.allowed-origins:https://localhost:5174}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "APIKey")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/**")  // Intercept all paths
                .excludePathPatterns(
                        "/auth/sso/**",          // SSO (OIDC/BFF) login flow
                        "/api/auth/sso/**"
                );
    }
}