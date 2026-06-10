package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Base class for file entities (PKSI files and FS2 files).
 * Consolidates common file metadata fields to follow DRY principle.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseFile extends BaseEntity {

    @Column(name = "file_name", nullable = false, length = 256)
    private String fileName;

    @Column(name = "original_name", nullable = false, length = 256)
    private String originalName;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "blob_url", length = 512)
    private String blobUrl;

    @Column(name = "blob_name", length = 512)
    private String blobName;

    @Column(name = "session_id", length = 128)
    private String sessionId;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "version")
    @lombok.Builder.Default
    private Integer version = 1;

    @Column(name = "file_group_id")
    private UUID fileGroupId;

    @Column(name = "display_name", length = 512)
    private String displayName;

    @Column(name = "tanggal_dokumen")
    private LocalDate tanggalDokumen;
}
