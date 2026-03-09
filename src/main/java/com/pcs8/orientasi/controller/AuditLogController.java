package com.pcs8.orientasi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.response.AuditLogResponse;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.entity.AuditLog;
import com.pcs8.orientasi.domain.enums.AuditAction;
import com.pcs8.orientasi.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller untuk Audit Log API.
 * 
 * <p>Menyediakan endpoint untuk:</p>
 * <ul>
 *   <li>GET /audit-logs - List semua audit logs dengan pagination</li>
 *   <li>GET /audit-logs/search - Search audit logs dengan filter</li>
 *   <li>GET /audit-logs/{id} - Get detail audit log by ID</li>
 *   <li>GET /audit-logs/entity/{entityName}/{entityId} - Get audit logs untuk entity tertentu</li>
 *   <li>GET /audit-logs/entity/{entityName} - Get audit logs untuk entity type</li>
 *   <li>GET /audit-logs/user/{userId} - Get audit logs untuk user tertentu</li>
 *   <li>GET /audit-logs/statistics - Get statistik audit logs</li>
 * </ul>
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@RequiresRole({"admin"})
public class AuditLogController {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    /**
     * Get semua audit logs dengan pagination.
     */
    @GetMapping
    public ResponseEntity<BaseResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> pageResult = auditService.getAllAuditLogs(pageable);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                buildPageResponse(pageResult)
        ));
    }

    /**
     * Search audit logs dengan multiple filter.
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse> search(
            @RequestParam(name = "entity_name", required = false) String entityName,
            @RequestParam(name = "entity_id", required = false) UUID entityId,
            @RequestParam(required = false) String action,
            @RequestParam(name = "user_id", required = false) UUID userId,
            @RequestParam(required = false) String username,
            @RequestParam(name = "start_date", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "end_date", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        AuditAction auditAction = null;
        if (action != null && !action.isEmpty()) {
            try {
                auditAction = AuditAction.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse(HttpStatus.BAD_REQUEST.value(), 
                                "Invalid action. Use: CREATE, UPDATE, or DELETE", null));
            }
        }
        
        Page<AuditLog> pageResult = auditService.searchAuditLogs(
                entityName, entityId, auditAction, userId, username, startDate, endDate, pageable);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                buildPageResponse(pageResult)
        ));
    }

    /**
     * Get audit log by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        AuditLog auditLog = auditService.getAuditLogById(id);
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                mapToResponse(auditLog)
        ));
    }

    /**
     * Get audit logs untuk entity tertentu (by entity name dan entity id).
     */
    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<BaseResponse> getByEntity(
            @PathVariable String entityName,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> pageResult = auditService.getAuditLogsByEntity(entityName, entityId, pageable);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                buildPageResponse(pageResult)
        ));
    }

    /**
     * Get recent 10 audit logs untuk entity tertentu.
     */
    @GetMapping("/entity/{entityName}/{entityId}/recent")
    public ResponseEntity<BaseResponse> getRecentByEntity(
            @PathVariable String entityName,
            @PathVariable UUID entityId
    ) {
        List<AuditLog> auditLogs = auditService.getRecentAuditLogs(entityName, entityId);
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                responses
        ));
    }

    /**
     * Get audit logs untuk entity type (hanya berdasarkan entity name).
     */
    @GetMapping("/entity/{entityName}")
    public ResponseEntity<BaseResponse> getByEntityName(
            @PathVariable String entityName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> pageResult = auditService.getAuditLogsByEntityName(entityName, pageable);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                buildPageResponse(pageResult)
        ));
    }

    /**
     * Get audit logs untuk user tertentu.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> pageResult = auditService.getAuditLogsByUser(userId, pageable);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                buildPageResponse(pageResult)
        ));
    }

    /**
     * Get statistik audit logs.
     */
    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse> getStatistics() {
        Map<String, Object> stats = auditService.getAuditStatistics();
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                stats
        ));
    }

    /**
     * Get distinct entity names.
     */
    @GetMapping("/entities")
    public ResponseEntity<BaseResponse> getDistinctEntities() {
        List<String> entityNames = auditService.getDistinctEntityNames();
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                entityNames
        ));
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> buildPageResponse(Page<AuditLog> pageResult) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        return responseData;
    }

    private AuditLogResponse mapToResponse(AuditLog entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .entityName(entity.getEntityName())
                .entityId(entity.getEntityId())
                .action(entity.getAction().name())
                .oldValue(parseJson(entity.getOldValue()))
                .newValue(parseJson(entity.getNewValue()))
                .changedFields(parseJson(entity.getChangedFields()))
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json; // Return as-is if not valid JSON
        }
    }
}
