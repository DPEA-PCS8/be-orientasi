package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class untuk entity file yang disimpan di Minio Storage.
 * Berisi field-field umum yang digunakan oleh Fs2File dan PksiFile.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseFileEntity extends BaseEntity {

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
    private String sessionId;
}
