package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity untuk dokumen T.01 (PKSI) - Full Version
 * Mendukung semua field dari form frontend
 */
@Entity
@Table(name = "trn_pksi_document")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PksiDocument extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MstUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_id")
    private MstAplikasi aplikasi;

    // ==================== HEADER ====================
    @Column(name = "nama_pksi", nullable = false, length = 255)
    private String namaPksi;

    @Column(name = "tanggal_pengajuan")
    private LocalDate tanggalPengajuan;

    // ==================== SECTION 1: PENDAHULUAN ====================
    @Column(name = "deskripsi_pksi", columnDefinition = "NVARCHAR(MAX)")
    private String deskripsiPksi;

    @Column(name = "mengapa_pksi_diperlukan", columnDefinition = "NVARCHAR(MAX)")
    private String mengapaPksiDiperlukan;

    @Column(name = "kapan_diselesaikan", length = 500)
    private String kapanDiselesaikan;

    @Column(name = "pic_satker", length = 255)
    private String picSatker;

    // ==================== SECTION 2: TUJUAN DAN KEGUNAAN ====================
    @Column(name = "kegunaan_pksi", columnDefinition = "NVARCHAR(MAX)")
    private String kegunaanPksi;

    @Column(name = "tujuan_pksi", columnDefinition = "NVARCHAR(MAX)")
    private String tujuanPksi;

    @Column(name = "target_pksi", columnDefinition = "NVARCHAR(MAX)")
    private String targetPksi;

    // ==================== SECTION 3: CAKUPAN ====================
    @Column(name = "ruang_lingkup", columnDefinition = "NVARCHAR(MAX)")
    private String ruangLingkup;

    @Column(name = "batasan_pksi", columnDefinition = "NVARCHAR(MAX)")
    private String batasanPksi;

    @Column(name = "hubungan_sistem_lain", columnDefinition = "NVARCHAR(MAX)")
    private String hubunganSistemLain;

    @Column(name = "asumsi", columnDefinition = "NVARCHAR(MAX)")
    private String asumsi;

    // ==================== SECTION 4: RISIKO DAN BATASAN ====================
    @Column(name = "batasan_desain", columnDefinition = "NVARCHAR(MAX)")
    private String batasanDesain;

    @Column(name = "risiko_bisnis", columnDefinition = "NVARCHAR(MAX)")
    private String risikoBisnis;

    @Column(name = "risiko_sukses_pksi", columnDefinition = "NVARCHAR(MAX)")
    private String risikoSuksesPksi;

    @Column(name = "pengendalian_risiko", columnDefinition = "NVARCHAR(MAX)")
    private String pengendalianRisiko;

    // ==================== SECTION 5: GAMBARAN UMUM APLIKASI ====================
    @Column(name = "pengelola_aplikasi", length = 255)
    private String pengelolaAplikasi;

    @Column(name = "pengguna_aplikasi", columnDefinition = "NVARCHAR(MAX)")
    private String penggunaAplikasi;

    @Column(name = "program_inisiatif_rbsi", length = 255)
    private String programInisiatifRbsi;

    @Column(name = "fungsi_aplikasi", columnDefinition = "NVARCHAR(MAX)")
    private String fungsiAplikasi;

    @Column(name = "informasi_yang_dikelola", columnDefinition = "NVARCHAR(MAX)")
    private String informasiYangDikelola;

    @Column(name = "dasar_peraturan", columnDefinition = "NVARCHAR(MAX)")
    private String dasarPeraturan;

    // ==================== SECTION 6: USULAN JADWAL PELAKSANAAN ====================
    // Tahap 1: Penyusunan Spesifikasi
    @Column(name = "tahap1_awal")
    private LocalDate tahap1Awal;

    @Column(name = "tahap1_akhir")
    private LocalDate tahap1Akhir;

    // Tahap 5: UAT
    @Column(name = "tahap5_awal")
    private LocalDate tahap5Awal;

    @Column(name = "tahap5_akhir")
    private LocalDate tahap5Akhir;

    // Tahap 7: Go-Live
    @Column(name = "tahap7_awal")
    private LocalDate tahap7Awal;

    @Column(name = "tahap7_akhir")
    private LocalDate tahap7Akhir;

    // ==================== SECTION 7: RENCANA PENGELOLAAN ====================
    @Column(name = "rencana_pengelolaan", columnDefinition = "NVARCHAR(MAX)")
    private String rencanaPengelolaan;

    // ==================== STATUS ====================
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    // ==================== LEGACY FIELDS (backward compatibility) ====================
    @Column(name = "tujuan_pengajuan", columnDefinition = "NVARCHAR(MAX)")
    private String tujuanPengajuan;

    public enum DocumentStatus {
        PENDING,
        DISETUJUI,
        DITOLAK,
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        REVISION
    }
}
