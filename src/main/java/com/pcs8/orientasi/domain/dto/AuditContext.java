package com.pcs8.orientasi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO untuk menyimpan informasi user context untuk audit logging.
 * 
 * <p>Class ini digunakan untuk capture user info di main thread
 * sebelum dikirim ke async audit service, karena request context
 * tidak tersedia di async thread.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditContext {
    
    private UUID userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    
    /**
     * Create default context untuk system operations.
     */
    public static AuditContext system() {
        return AuditContext.builder()
                .username("system")
                .build();
    }
}
