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

import static com.pcs8.orientasi.domain.constants.AplikasiFieldNames.*;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_snapshot", uniqueConstraints = {
    @UniqueConstraint(columnNames = {APLIKASI_ID, TAHUN})
})
public class AplikasiSnapshot extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = ID, updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = APLIKASI_ID, nullable = false)
    @ToString.Exclude
    private MstAplikasi aplikasi;

    @Column(name = TAHUN, nullable = false)
    private Integer tahun;

    // Copy of aplikasi fields at snapshot time
    @Column(name = KODE_APLIKASI, nullable = false, length = 50)
    private String kodeAplikasi;

    @Column(name = NAMA_APLIKASI, nullable = false, length = 255)
    private String namaAplikasi;

    @Column(name = DESKRIPSI, columnDefinition = "NVARCHAR(MAX)")
    private String deskripsi;

    @Column(name = STATUS_APLIKASI, nullable = false, length = 20)
    private String statusAplikasi;

    @Column(name = TANGGAL_STATUS)
    private LocalDate tanggalStatus;

    @Column(name = BIDANG_ID)
    private UUID bidangId;

    @Column(name = BIDANG_KODE, length = 50)
    private String bidangKode;

    @Column(name = BIDANG_NAMA, length = 255)
    private String bidangNama;

    @Column(name = SKPA_ID)
    private UUID skpaId;

    @Column(name = SKPA_KODE, length = 50)
    private String skpaKode;

    @Column(name = SKPA_NAMA, length = 255)
    private String skpaNama;

    @Column(name = TANGGAL_IMPLEMENTASI)
    private LocalDate tanggalImplementasi;

    @Column(name = AKSES, length = 20)
    private String akses;

    @Column(name = PROSES_DATA_PRIBADI, nullable = false)
    @Builder.Default
    private Boolean prosesDataPribadi = false;

    @Column(name = DATA_PRIBADI_DIPROSES, columnDefinition = "NVARCHAR(MAX)")
    private String dataPribadiDiproses;

    // Idle-specific fields
    @Column(name = KATEGORI_IDLE, length = 100)
    private String kategoriIdle;

    @Column(name = ALASAN_IDLE, columnDefinition = "NVARCHAR(MAX)")
    private String alasanIdle;

    @Column(name = RENCANA_PENGAKHIRAN, columnDefinition = "NVARCHAR(MAX)")
    private String rencanaPengakhiran;

    @Column(name = ALASAN_BELUM_DIAKHIRI, columnDefinition = "NVARCHAR(MAX)")
    private String alasanBelumDiakhiri;

    // Snapshot metadata
    @Column(name = SNAPSHOT_DATE, nullable = false)
    private LocalDateTime snapshotDate;

    @Column(name = SNAPSHOT_TYPE, nullable = false, length = 20)
    @Builder.Default
    private String snapshotType = "AUTO"; // AUTO, MANUAL

    @Column(name = KETERANGAN_HISTORIS, columnDefinition = "NVARCHAR(MAX)")
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
