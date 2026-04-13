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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inisiatif_group_id")
    private InisiatifGroup inisiatifGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inisiatif_id")
    private RbsiInisiatif inisiatif;

    // ==================== HEADER ====================
    @Column(name = "nama_pksi", nullable = false, length = 255)
    private String namaPksi;

    @Column(name = "jenis_pksi", length = 50)
    private String jenisPksi;

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

    // ==================== SECTION 7: RENCANA PENGELOLAAN ====================
    @Column(name = "rencana_pengelolaan", columnDefinition = "NVARCHAR(MAX)")
    private String rencanaPengelolaan;

    // ==================== STATUS ====================
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    // ==================== APPROVAL FIELDS (set when status = DISETUJUI) ====================
    @Column(name = "iku", length = 10)
    private String iku;

    @Column(name = "inhouse_outsource", length = 20)
    private String inhouseOutsource;

    @Column(name = "pic_approval", length = 255)
    private String picApproval;

    @Column(name = "pic_approval_name", length = 255)
    private String picApprovalName;

    @Column(name = "anggota_tim", columnDefinition = "NVARCHAR(MAX)")
    private String anggotaTim;

    @Column(name = "anggota_tim_names", columnDefinition = "NVARCHAR(MAX)")
    private String anggotaTimNames;

    @Column(name = "progress", length = 50)
    private String progress;

    // ==================== MONITORING FIELDS ====================
    @Column(name = "anggaran_total", length = 255)
    private String anggaranTotal;

    @Column(name = "anggaran_tahun_ini", length = 255)
    private String anggaranTahunIni;

    @Column(name = "anggaran_tahun_depan", length = 255)
    private String anggaranTahunDepan;

    @Column(name = "target_usreq")
    private LocalDate targetUsreq;

    @Column(name = "target_sit")
    private LocalDate targetSit;

    @Column(name = "target_uat")
    private LocalDate targetUat;

    @Column(name = "target_go_live")
    private LocalDate targetGoLive;

    @Column(name = "status_t01_t02", length = 50)
    private String statusT01T02;

    @Column(name = "berkas_t01_t02", length = 255)
    private String berkasT01T02;

    @Column(name = "status_t11", length = 50)
    private String statusT11;

    @Column(name = "berkas_t11", length = 255)
    private String berkasT11;

    @Column(name = "status_cd", length = 50)
    private String statusCd;

    @Column(name = "nomor_cd", length = 100)
    private String nomorCd;

    @Column(name = "kontrak_tanggal_mulai", length = 50)
    private String kontrakTanggalMulai;

    @Column(name = "kontrak_tanggal_selesai", length = 50)
    private String kontrakTanggalSelesai;

    @Column(name = "kontrak_nilai", length = 255)
    private String kontrakNilai;

    @Column(name = "kontrak_jumlah_termin", length = 50)
    private String kontrakJumlahTermin;

    @Column(name = "kontrak_detail_pembayaran", columnDefinition = "NVARCHAR(MAX)")
    private String kontrakDetailPembayaran;

    @Column(name = "ba_deploy", length = 255)
    private String baDeploy;

    // ==================== TEAM REFERENCE ====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private MstTeam team;

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
