package com.pcs8.orientasi.domain.dto.request;

import com.pcs8.orientasi.domain.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kriteria pencarian untuk Audit Log.
 * Mengenkapsulasi semua filter parameters untuk mengurangi method signature complexity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchCriteria {
    
    private String entityName;
    
    private UUID entityId;
    
    private AuditAction action;
    
    private UUID userId;
    
    private String username;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
}
