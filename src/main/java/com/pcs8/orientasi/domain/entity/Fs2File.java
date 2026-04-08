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
 * Entity untuk file lampiran F.S.2 yang disimpan di Minio Storage.
 */
@Entity
@Table(name = "trn_fs2_file")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Fs2File extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fs2_id", nullable = true)
    private Fs2Document fs2Document;

    // File metadata fields for FS2 attachments stored in MinIO
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
    private String fileType; // ND, FS2, CD, FS2A, FS2B, F45, F46, NDBA

    @Column(name = "version")
    @lombok.Builder.Default
    private Integer version = 1;

    @Column(name = "file_group_id")
    private UUID fileGroupId;

    @Column(name = "display_name", length = 512)
    private String displayName;
}
