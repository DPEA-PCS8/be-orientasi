package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.dto.request.AuditLogSearchCriteria;
import com.pcs8.orientasi.domain.entity.AuditLog;
import com.pcs8.orientasi.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository untuk AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Cari audit log berdasarkan entity name dan entity id (dengan pagination).
     */
    Page<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(
            String entityName, UUID entityId, Pageable pageable);

    /**
     * Cari audit log berdasarkan entity name dan entity id (tanpa pagination).
     */
    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(
            String entityName, UUID entityId);

    /**
     * Cari audit log berdasarkan entity name saja.
     */
    Page<AuditLog> findByEntityNameOrderByCreatedAtDesc(String entityName, Pageable pageable);

    /**
     * Cari audit log berdasarkan user id.
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Cari audit log berdasarkan action type.
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    /**
     * Cari audit log berdasarkan rentang waktu.
     */
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Cari 10 audit log terbaru untuk entity tertentu.
     */
    List<AuditLog> findTop10ByEntityNameAndEntityIdOrderByCreatedAtDesc(
            String entityName, UUID entityId);

    /**
     * Search audit log dengan multiple filter.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:#{#criteria.entityName} IS NULL OR a.entityName = :#{#criteria.entityName}) AND " +
           "(:#{#criteria.entityId} IS NULL OR a.entityId = :#{#criteria.entityId}) AND " +
           "(:#{#criteria.action} IS NULL OR a.action = :#{#criteria.action}) AND " +
           "(:#{#criteria.userId} IS NULL OR a.userId = :#{#criteria.userId}) AND " +
           "(:#{#criteria.username} IS NULL OR a.username LIKE %:#{#criteria.username}%) AND " +
           "(:#{#criteria.startDate} IS NULL OR a.createdAt >= :#{#criteria.startDate}) AND " +
           "(:#{#criteria.endDate} IS NULL OR a.createdAt <= :#{#criteria.endDate})")
    Page<AuditLog> searchAuditLogs(
            @Param("criteria") AuditLogSearchCriteria criteria,
            Pageable pageable);

    /**
     * Count audit logs per entity name.
     */
    @Query("SELECT a.entityName, COUNT(a) FROM AuditLog a GROUP BY a.entityName ORDER BY COUNT(a) DESC")
    List<Object[]> countByEntityName();

    /**
     * Count audit logs per action.
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action")
    List<Object[]> countByAction();

    /**
     * Get distinct entity names.
     */
    @Query("SELECT DISTINCT a.entityName FROM AuditLog a ORDER BY a.entityName")
    List<String> findDistinctEntityNames();
}
