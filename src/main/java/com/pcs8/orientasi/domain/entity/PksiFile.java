package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entity untuk file lampiran PKSI yang disimpan di Minio Storage
 */
@Entity
@Table(name = "trn_pksi_file")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PksiFile extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pksi_id", nullable = true) // Nullable for temp files
    private PksiDocument pksiDocument;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "blob_url", length = 500)
    private String blobUrl;

    @Column(name = "blob_name", length = 500)
    private String blobName;

    @Column(name = "session_id", length = 100)
    private String sessionId; // For temporary files before PKSI association

    @Column(name = "file_type", length = 20)
    private String fileType; // T01 = Rencana PKSI, T11 = Spesifikasi Kebutuhan

    @Column(name = "version")
    @lombok.Builder.Default
    private Integer version = 1;

    @Column(name = "file_group_id")
    private UUID fileGroupId;

    @Column(name = "display_name", length = 512)
    private String displayName;
}
