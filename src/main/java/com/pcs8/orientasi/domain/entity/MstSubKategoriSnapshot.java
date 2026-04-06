package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_sub_kategori_snapshot", indexes = {
    @Index(name = "idx_snapshot_year", columnList = "snapshot_year"),
    @Index(name = "idx_snapshot_sub_kategori", columnList = "sub_kategori_id")
})
public class MstSubKategoriSnapshot {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "snapshot_year", nullable = false)
    private Integer snapshotYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_kategori_id", nullable = false)
    private MstSubKategori subKategori;

    @Column(name = "kode", nullable = false, length = 20)
    private String kode;

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Column(name = "category_code", nullable = false, length = 10)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // CREATED, UPDATED, DELETED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (snapshotDate == null) {
            snapshotDate = LocalDateTime.now();
        }
    }
}
