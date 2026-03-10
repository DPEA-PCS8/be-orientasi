package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_snapshot", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"aplikasi_id", "tahun"})
})
public class AplikasiSnapshot extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_id", nullable = false)
    @ToString.Exclude
    private MstAplikasi aplikasi;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    // Copy of aplikasi fields at snapshot time
    @Column(name = "kode_aplikasi", nullable = false, length = 50)
    private String kodeAplikasi;

    @Column(name = "nama_aplikasi", nullable = false, length = 255)
    private String namaAplikasi;

    @Column(name = "deskripsi", columnDefinition = "NVARCHAR(MAX)")
    private String deskripsi;

    @Column(name = "status_aplikasi", nullable = false, length = 20)
    private String statusAplikasi;

    @Column(name = "tanggal_status")
    private LocalDate tanggalStatus;

    @Column(name = "bidang_id")
    private UUID bidangId;

    @Column(name = "bidang_kode", length = 50)
    private String bidangKode;

    @Column(name = "bidang_nama", length = 255)
    private String bidangNama;

    @Column(name = "skpa_id")
    private UUID skpaId;

    @Column(name = "skpa_kode", length = 50)
    private String skpaKode;

    @Column(name = "skpa_nama", length = 255)
    private String skpaNama;

    @Column(name = "tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @Column(name = "akses", length = 20)
    private String akses;

    @Column(name = "proses_data_pribadi", nullable = false)
    @Builder.Default
    private Boolean prosesDataPribadi = false;

    @Column(name = "data_pribadi_diproses", columnDefinition = "NVARCHAR(MAX)")
    private String dataPribadiDiproses;

    // Idle-specific fields
    @Column(name = "kategori_idle", length = 100)
    private String kategoriIdle;

    @Column(name = "alasan_idle", columnDefinition = "NVARCHAR(MAX)")
    private String alasanIdle;

    @Column(name = "rencana_pengakhiran", columnDefinition = "NVARCHAR(MAX)")
    private String rencanaPengakhiran;

    @Column(name = "alasan_belum_diakhiri", columnDefinition = "NVARCHAR(MAX)")
    private String alasanBelumDiakhiri;

    // Snapshot metadata
    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;

    @Column(name = "snapshot_type", nullable = false, length = 20)
    @Builder.Default
    private String snapshotType = "AUTO"; // AUTO, MANUAL

    @Column(name = "keterangan_historis", columnDefinition = "NVARCHAR(MAX)")
    private String keteranganHistoris;

    // One-to-many relations
    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AplikasiSnapshotUrl> urls = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AplikasiSnapshotSatkerInternal> satkerInternals = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AplikasiSnapshotPenggunaEksternal> penggunaEksternals = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AplikasiSnapshotKomunikasiSistem> komunikasiSistems = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AplikasiSnapshotPenghargaan> penghargaans = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AplikasiChangelog> changelogs = new ArrayList<>();
}
