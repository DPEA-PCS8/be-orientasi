package com.pcs8.orientasi.config;

import com.pcs8.orientasi.domain.dto.AuditContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Utility class untuk mendapatkan informasi user dari request context.
 * 
 * <p>Class ini mengambil informasi user yang sudah di-set oleh
 * {@link AuthorizationInterceptor} dari JWT token.</p>
 * 
 * <p>Informasi yang tersedia:</p>
 * <ul>
 *   <li>User UUID</li>
 *   <li>Username (dari JWT claims)</li>
 *   <li>IP Address</li>
 *   <li>User Agent</li>
 * </ul>
 */
@Slf4j
@Component
public class UserContext {

    /**
     * Get current HttpServletRequest dari RequestContextHolder.
     */
    public HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get UUID user yang sedang login dari request attribute.
     * Attribute ini di-set oleh AuthorizationInterceptor.
     */
    public UUID getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String userUuidStr = (String) request.getAttribute("user_uuid");
        if (userUuidStr == null || userUuidStr.isEmpty()) {
            return null;
        }
        
        try {
            return UUID.fromString(userUuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get username dari JWT token.
     * Username di-set oleh AuthorizationInterceptor dari JWT subject.
     */
    public String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "system";
        }
        
        // Username sudah di-set oleh AuthorizationInterceptor
        String username = (String) request.getAttribute("username");
        if (username != null && !username.isEmpty()) {
            return username;
        }
        
        // Fallback ke system jika tidak ada username
        return "system";
    }

    /**
     * Get IP address dari client.
     * Menghandle proxy dengan memeriksa X-Forwarded-For header.
     */
    public String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For bisa berisi multiple IP, ambil yang pertama
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Get User-Agent header.
     */
    public String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String userAgent = request.getHeader("User-Agent");
        // Truncate jika terlalu panjang
        if (userAgent != null && userAgent.length() > 500) {
            userAgent = userAgent.substring(0, 500);
        }
        return userAgent;
    }

    /**
     * Capture current user context sebagai AuditContext snapshot.
     * 
     * <p>Method ini harus dipanggil di main thread (HTTP request thread)
     * sebelum mengirim ke async audit service.</p>
     * 
     * @return AuditContext berisi user info yang sudah di-capture
     */
    public AuditContext captureContext() {
        return AuditContext.builder()
                .userId(getCurrentUserId())
                .username(getCurrentUsername())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .build();
    }
}
