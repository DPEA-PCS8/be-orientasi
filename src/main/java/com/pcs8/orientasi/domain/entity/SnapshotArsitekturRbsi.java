package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "snapshot_arsitektur_rbsi", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rbsi_id", "snapshot_date", "arsitektur_id"})
})
public class SnapshotArsitekturRbsi extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    @ToString.Exclude
    private Rbsi rbsi;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    // Snapshot dari arsitektur_id asal
    @Column(name = "arsitektur_id", nullable = false)
    private UUID arsitekturId;

    // Denormalized fields untuk snapshot immutable
    @Column(name = "sub_kategori_kode", length = 20)
    private String subKategoriKode;

    @Column(name = "sub_kategori_nama", length = 255)
    private String subKategoriNama;

    @Column(name = "aplikasi_kode", length = 50)
    private String aplikasiKode;

    @Column(name = "aplikasi_nama", length = 255)
    private String aplikasiNama;

    @Column(name = "aplikasi_baseline_kode", length = 50)
    private String aplikasiBaselineKode;

    @Column(name = "aplikasi_baseline_nama", length = 255)
    private String aplikasiBaselineNama;

    @Column(name = "aplikasi_target_kode", length = 50)
    private String aplikasiTargetKode;

    @Column(name = "aplikasi_target_nama", length = 255)
    private String aplikasiTargetNama;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "year_statuses", length = 500)
    private String yearStatuses;

    @Column(name = "inisiatif_group_id")
    private UUID inisiatifGroupId;

    @Column(name = "inisiatif_group_nama", length = 255)
    private String inisiatifGroupNama;

    @Column(name = "skpa_kode", length = 50)
    private String skpaKode;

    @Column(name = "skpa_nama", length = 255)
    private String skpaNama;

    @Column(name = "keterangan", columnDefinition = "TEXT")
    private String keterangan;

    // Perubahan yang terjadi pada update data (JSON: [{field, oldValue, newValue}])
    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;
}
