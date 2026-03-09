package com.pcs8.orientasi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcs8.orientasi.domain.entity.AuditLog;
import com.pcs8.orientasi.domain.enums.AuditAction;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.AuditLogRepository;
import com.pcs8.orientasi.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementasi AuditService untuk mencatat dan mengquery audit log.
 * 
 * <p>Fitur utama:</p>
 * <ul>
 *   <li>Async logging untuk tidak memblok main transaction</li>
 *   <li>REQUIRES_NEW propagation untuk memastikan audit tersimpan meski main transaction rollback</li>
 *   <li>User info (userId & username) di-pass sebagai parameter dari main thread</li>
 *   <li>JSON serialization untuk old/new values</li>
 *   <li>Automatic changed fields detection</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    // ==================== LOGGING METHODS ====================

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(String entityName, UUID entityId, Object newValue, UUID userId, String username) {
        try {
            AuditLog auditLog = buildAuditLog(entityName, entityId, AuditAction.CREATE, userId, username);
            auditLog.setNewValue(toJson(newValue));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} - CREATE by {}", 
                    entityName, entityId, username);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}", 
                    entityName, entityId, e.getMessage(), e);
        }
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(String entityName, UUID entityId, Object oldValue, Object newValue, UUID userId, String username) {
        try {
            // Convert to JSON for comparison
            String oldJson = toJson(oldValue);
            String newJson = toJson(newValue);
            
            // Skip logging if nothing changed
            if (Objects.equals(oldJson, newJson)) {
                log.debug("Skipping audit log for {} {} - no changes detected", entityName, entityId);
                return;
            }
            
            AuditLog auditLog = buildAuditLog(entityName, entityId, AuditAction.UPDATE, userId, username);
            auditLog.setOldValue(oldJson);
            auditLog.setNewValue(newJson);
            auditLog.setChangedFields(calculateChangedFields(oldValue, newValue));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} - UPDATE by {}", 
                    entityName, entityId, username);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}", 
                    entityName, entityId, e.getMessage(), e);
        }
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDelete(String entityName, UUID entityId, Object oldValue, UUID userId, String username) {
        try {
            AuditLog auditLog = buildAuditLog(entityName, entityId, AuditAction.DELETE, userId, username);
            auditLog.setOldValue(toJson(oldValue));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} - DELETE by {}", 
                    entityName, entityId, username);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}", 
                    entityName, entityId, e.getMessage(), e);
        }
    }

    // ==================== QUERY METHODS ====================

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByEntity(String entityName, UUID entityId, Pageable pageable) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(
                entityName, entityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByEntityName(String entityName, Pageable pageable) {
        return auditLogRepository.findByEntityNameOrderByCreatedAtDesc(entityName, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(
            String entityName,
            UUID entityId,
            AuditAction action,
            UUID userId,
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.searchAuditLogs(
                entityName, entityId, action, userId, username, startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAuditLogs(String entityName, UUID entityId) {
        return auditLogRepository.findTop10ByEntityNameAndEntityIdOrderByCreatedAtDesc(
                entityName, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog getAuditLogById(UUID id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log tidak ditemukan"));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total count
        stats.put("total_count", auditLogRepository.count());
        
        // Count by entity
        List<Object[]> entityCounts = auditLogRepository.countByEntityName();
        Map<String, Long> byEntity = new LinkedHashMap<>();
        for (Object[] row : entityCounts) {
            byEntity.put((String) row[0], (Long) row[1]);
        }
        stats.put("by_entity", byEntity);
        
        // Count by action
        List<Object[]> actionCounts = auditLogRepository.countByAction();
        Map<String, Long> byAction = new LinkedHashMap<>();
        for (Object[] row : actionCounts) {
            byAction.put(((AuditAction) row[0]).name(), (Long) row[1]);
        }
        stats.put("by_action", byAction);
        
        return stats;
    }

    @Override
    public List<String> getDistinctEntityNames() {
        return auditLogRepository.findDistinctEntityNames();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Build AuditLog object dengan informasi user yang sudah di-pass.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity
     * @param action Aksi yang dilakukan
     * @param userId ID user yang melakukan aksi
     * @param username Username yang melakukan aksi
     */
    private AuditLog buildAuditLog(String entityName, UUID entityId, AuditAction action, UUID userId, String username) {
        return AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .userId(userId)
                .username(username)
                .ipAddress(null) // Simplified - IP tracking removed
                .userAgent(null) // Simplified - User agent tracking removed
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Convert object ke JSON string.
     */
    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Calculate changed fields antara old dan new value.
     * Mengembalikan JSON array dari field names yang berubah.
     */
    @SuppressWarnings("unchecked")
    private String calculateChangedFields(Object oldValue, Object newValue) {
        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldValue, Map.class);
            Map<String, Object> newMap = objectMapper.convertValue(newValue, Map.class);
            
            Set<String> changedFields = new LinkedHashSet<>();
            
            // Check all keys in new map
            for (String key : newMap.keySet()) {
                Object oldVal = oldMap.get(key);
                Object newVal = newMap.get(key);
                
                if (!Objects.equals(oldVal, newVal)) {
                    changedFields.add(key);
                }
            }
            
            // Check for removed keys
            for (String key : oldMap.keySet()) {
                if (!newMap.containsKey(key)) {
                    changedFields.add(key);
                }
            }
            
            return objectMapper.writeValueAsString(changedFields);
        } catch (Exception e) {
            log.error("Failed to calculate changed fields: {}", e.getMessage());
            return "[]";
        }
    }
}
