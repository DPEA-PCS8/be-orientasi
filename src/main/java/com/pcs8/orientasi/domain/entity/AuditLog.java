package com.pcs8.orientasi.domain.entity;

import com.pcs8.orientasi.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity untuk menyimpan audit log dari semua operasi CRUD di sistem.
 * 
 * <p>Setiap record mencatat:</p>
 * <ul>
 *   <li>Entity apa yang dimodifikasi</li>
 *   <li>ID dari entity tersebut</li>
 *   <li>Jenis aksi (CREATE, UPDATE, DELETE)</li>
 *   <li>Data sebelum dan sesudah perubahan (dalam format JSON)</li>
 *   <li>Field-field yang berubah</li>
 *   <li>Informasi user yang melakukan perubahan</li>
 *   <li>IP address dan user agent</li>
 * </ul>
 */
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_name, entity_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Nama entity yang diaudit (e.g., "Aplikasi", "Bidang", "User")
     */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    /**
     * ID dari entity yang diaudit
     */
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /**
     * Jenis aksi: CREATE, UPDATE, DELETE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    /**
     * Nilai sebelum perubahan (JSON format)
     * Null untuk CREATE action
     */
    @Column(name = "old_value", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    /**
     * Nilai setelah perubahan (JSON format)
     * Null untuk DELETE action
     */
    @Column(name = "new_value", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    /**
     * List field yang berubah (JSON array)
     * Hanya untuk UPDATE action
     */
    @Column(name = "changed_fields", columnDefinition = "NVARCHAR(MAX)")
    private String changedFields;

    /**
     * ID user yang melakukan perubahan
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Username yang melakukan perubahan
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * IP address dari user
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent (browser/client info)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Waktu audit log dibuat
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
