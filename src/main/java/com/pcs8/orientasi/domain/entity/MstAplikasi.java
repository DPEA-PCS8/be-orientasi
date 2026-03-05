package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mst_aplikasi", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kode_aplikasi"})
})
public class MstAplikasi extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kode_aplikasi", nullable = false, length = 50)
    private String kodeAplikasi;

    @Column(name = "nama_aplikasi", nullable = false, length = 255)
    private String namaAplikasi;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "status_aplikasi", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'AKTIF'")
    @Builder.Default
    private String statusAplikasi = "AKTIF"; // AKTIF, IDLE, DIAKHIRI

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidang_id", nullable = true)
    private MstBidang bidang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skpa_id", nullable = true)
    private MstSkpa skpa;

    @Column(name = "akses", length = 20)
    private String akses; // INTERNET, INTRANET, BOTH

    @Column(name = "proses_data_pribadi", nullable = false, columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private Boolean prosesDataPribadi = false;

    @Column(name = "data_pribadi_diproses", columnDefinition = "TEXT")
    private String dataPribadiDiproses;

    // Idle-specific fields
    @Column(name = "kategori_idle", length = 100)
    private String kategoriIdle; // TIDAK_DIGUNAKAN, HISTORIS, LAINNYA

    @Column(name = "alasan_idle", columnDefinition = "TEXT")
    private String alasanIdle;

    @Column(name = "rencana_pengakhiran", columnDefinition = "TEXT")
    private String rencanaPengakhiran;

    @Column(name = "alasan_belum_diakhiri", columnDefinition = "TEXT")
    private String alasanBelumDiakhiri;

    // One-to-many relations
    @OneToMany(mappedBy = "aplikasi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AplikasiUrl> urls = new ArrayList<>();

    @OneToMany(mappedBy = "aplikasi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AplikasiSatkerInternal> satkerInternals = new ArrayList<>();

    @OneToMany(mappedBy = "aplikasi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AplikasiPenggunaEksternal> penggunaEksternals = new ArrayList<>();

    @OneToMany(mappedBy = "aplikasi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AplikasiKomunikasiSistem> komunikasiSistems = new ArrayList<>();
}
