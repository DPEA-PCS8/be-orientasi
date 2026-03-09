package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.entity.AuditLog;
import com.pcs8.orientasi.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface untuk operasi audit log.
 * 
 * <p>Menyediakan method untuk:</p>
 * <ul>
 *   <li>Mencatat audit log (CREATE, UPDATE, DELETE)</li>
 *   <li>Mengquery audit log dengan berbagai filter</li>
 *   <li>Mendapatkan statistik audit log</li>
 * </ul>
 */
public interface AuditService {

    /**
     * Log aksi CREATE.
     * 
     * @param entityName Nama entity (e.g., "Aplikasi", "Bidang")
     * @param entityId ID entity yang dibuat
     * @param newValue Object yang baru dibuat
     * @param userId ID user yang melakukan aksi
     * @param username Username yang melakukan aksi
     */
    void logCreate(String entityName, UUID entityId, Object newValue, UUID userId, String username);

    /**
     * Log aksi UPDATE.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity yang diupdate
     * @param oldValue Object sebelum update
     * @param newValue Object setelah update
     * @param userId ID user yang melakukan aksi
     * @param username Username yang melakukan aksi
     */
    void logUpdate(String entityName, UUID entityId, Object oldValue, Object newValue, UUID userId, String username);

    /**
     * Log aksi DELETE.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity yang dihapus
     * @param oldValue Object yang dihapus
     * @param userId ID user yang melakukan aksi
     * @param username Username yang melakukan aksi
     */
    void logDelete(String entityName, UUID entityId, Object oldValue, UUID userId, String username);

    /**
     * Get semua audit logs dengan pagination.
     */
    Page<AuditLog> getAllAuditLogs(Pageable pageable);

    /**
     * Get audit logs untuk entity tertentu.
     */
    Page<AuditLog> getAuditLogsByEntity(String entityName, UUID entityId, Pageable pageable);

    /**
     * Get audit logs untuk entity name saja (tanpa entity id).
     */
    Page<AuditLog> getAuditLogsByEntityName(String entityName, Pageable pageable);

    /**
     * Get audit logs untuk user tertentu.
     */
    Page<AuditLog> getAuditLogsByUser(UUID userId, Pageable pageable);

    /**
     * Search audit logs dengan multiple filter.
     */
    Page<AuditLog> searchAuditLogs(
            String entityName,
            UUID entityId,
            AuditAction action,
            UUID userId,
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get recent 10 audit logs untuk entity tertentu.
     */
    List<AuditLog> getRecentAuditLogs(String entityName, UUID entityId);

    /**
     * Get audit log by ID.
     */
    AuditLog getAuditLogById(UUID id);

    /**
     * Get statistik audit logs.
     */
    Map<String, Object> getAuditStatistics();

    /**
     * Get distinct entity names yang pernah tercatat di audit log.
     */
    List<String> getDistinctEntityNames();
}
