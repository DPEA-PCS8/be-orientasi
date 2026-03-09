package com.pcs8.orientasi.service;

import com.pcs8.orientasi.config.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Helper service untuk audit logging dengan pattern yang lebih clean.
 * 
 * <p>Service ini menyediakan method-method helper untuk melakukan
 * audit logging dengan mudah, terutama untuk operasi yang membutuhkan
 * capture old value sebelum operasi.</p>
 * 
 * <h3>Contoh Penggunaan:</h3>
 * <pre>{@code
 * // Untuk CREATE
 * public AplikasiResponse create(AplikasiRequest request) {
 *     MstAplikasi saved = repository.save(aplikasi);
 *     AplikasiResponse response = mapToResponse(saved);
 *     auditableService.auditCreate("Aplikasi", saved.getId(), response);
 *     return response;
 * }
 * 
 * // Untuk UPDATE (dengan capture old value)
 * public AplikasiResponse update(UUID id, AplikasiRequest request) {
 *     return auditableService.executeWithAudit(
 *         "Aplikasi",
 *         id,
 *         () -> mapToResponse(repository.findById(id).orElseThrow()),  // oldValue supplier
 *         () -> {
 *             // update logic
 *             MstAplikasi updated = repository.save(aplikasi);
 *             return mapToResponse(updated);
 *         }
 *     );
 * }
 * 
 * // Untuk DELETE
 * public void delete(UUID id) {
 *     MstAplikasi aplikasi = repository.findById(id).orElseThrow();
 *     AplikasiResponse oldValue = mapToResponse(aplikasi);
 *     repository.delete(aplikasi);
 *     auditableService.auditDelete("Aplikasi", id, oldValue);
 * }
 * }</pre>
 */
@Component
@RequiredArgsConstructor
public class AuditableService {

    private final AuditService auditService;
    private final UserContext userContext;

    /**
     * Log audit untuk CREATE action.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity yang baru dibuat
     * @param newValue Object yang baru dibuat
     */
    public void auditCreate(String entityName, UUID entityId, Object newValue) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        auditService.logCreate(entityName, entityId, newValue, userId, username);
    }

    /**
     * Log audit untuk UPDATE action.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity yang diupdate
     * @param oldValue Object sebelum update
     * @param newValue Object setelah update
     */
    public void auditUpdate(String entityName, UUID entityId, Object oldValue, Object newValue) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        auditService.logUpdate(entityName, entityId, oldValue, newValue, userId, username);
    }

    /**
     * Log audit untuk DELETE action.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity yang dihapus
     * @param oldValue Object yang dihapus
     */
    public void auditDelete(String entityName, UUID entityId, Object oldValue) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        auditService.logDelete(entityName, entityId, oldValue, userId, username);
    }

    /**
     * Execute operation dengan automatic audit logging.
     * Cocok untuk UPDATE operation yang perlu capture old value.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity
     * @param oldValueSupplier Supplier untuk mendapatkan old value
     * @param operation Operation yang akan dieksekusi
     * @return Result dari operation
     */
    public <T> T executeWithAudit(
            String entityName,
            UUID entityId,
            Supplier<T> oldValueSupplier,
            Supplier<T> operation) {
        
        // Get user info di main thread sebelum async call
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        // Capture old value sebelum operasi
        T oldValue = oldValueSupplier.get();
        
        // Execute operation
        T newValue = operation.get();
        
        // Log audit
        auditService.logUpdate(entityName, entityId, oldValue, newValue, userId, username);
        
        return newValue;
    }

    /**
     * Execute operation dengan automatic audit logging untuk CREATE.
     * 
     * @param entityName Nama entity
     * @param operation Operation yang akan dieksekusi (harus return object dengan getId())
     * @param idExtractor Function untuk extract ID dari result
     * @return Result dari operation
     */
    public <T> T executeCreate(
            String entityName,
            Supplier<T> operation,
            java.util.function.Function<T, UUID> idExtractor) {
        
        // Get user info di main thread sebelum async call
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        // Execute operation
        T result = operation.get();
        
        // Extract ID dan log audit
        UUID entityId = idExtractor.apply(result);
        auditService.logCreate(entityName, entityId, result, userId, username);
        
        return result;
    }

    /**
     * Execute DELETE dengan automatic audit logging.
     * 
     * @param entityName Nama entity
     * @param entityId ID entity yang akan dihapus
     * @param oldValue Object yang akan dihapus (sudah di-fetch sebelumnya)
     * @param deleteOperation Operation untuk delete
     */
    public void executeDelete(
            String entityName,
            UUID entityId,
            Object oldValue,
            Runnable deleteOperation) {
        
        // Get user info di main thread sebelum async call
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        // Execute delete
        deleteOperation.run();
        
        // Log audit
        auditService.logDelete(entityName, entityId, oldValue, userId, username);
    }
}
