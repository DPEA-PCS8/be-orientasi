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
@Table(name = "mst_fs2_document")
public class Fs2Document extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_name", length = 255)
    private String userName;

    // Relation to Aplikasi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_id")
    private MstAplikasi aplikasi;

    @Column(name = "tanggal_pengajuan")
    private LocalDate tanggalPengajuan;

    // Relation to Bidang
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidang_id")
    private MstBidang bidang;

    // Relation to SKPA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skpa_id")
    private MstSkpa skpa;

    // Status: PENDING, DISETUJUI, TIDAK_DISETUJUI
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    // === Form Fields from requirement point 6 ===
    
    // Deskripsi Pengubahan
    @Column(name = "deskripsi_pengubahan", columnDefinition = "TEXT")
    private String deskripsiPengubahan;

    // Alasan Pengubahan
    @Column(name = "alasan_pengubahan", columnDefinition = "TEXT")
    private String alasanPengubahan;

    // Status Tahapan Aplikasi: DESAIN, PEMELIHARAAN
    @Column(name = "status_tahapan", length = 50)
    private String statusTahapan;

    // Urgensi: RENDAH, SEDANG, TINGGI
    @Column(name = "urgensi", length = 50)
    private String urgensi;

    // Kesesuaian Kriteria Pengubahan Aplikasi (4 checkboxes stored as JSON or individual booleans)
    @Column(name = "kriteria_1")
    @Builder.Default
    private Boolean kriteria1 = false; // tidak menambah fungsi baru...
    
    @Column(name = "kriteria_2")
    @Builder.Default
    private Boolean kriteria2 = false; // tidak menambah sumber data baru...
    
    @Column(name = "kriteria_3")
    @Builder.Default
    private Boolean kriteria3 = false; // tidak mengubah sumber data...
    
    @Column(name = "kriteria_4")
    @Builder.Default
    private Boolean kriteria4 = false; // tidak mengubah alur kerja aplikasi

    // Aspek Perubahan
    @Column(name = "aspek_sistem_ada", columnDefinition = "TEXT")
    private String aspekSistemAda; // Terhadap sistem yang ada
    
    @Column(name = "aspek_sistem_terkait", columnDefinition = "TEXT")
    private String aspekSistemTerkait; // Terhadap sistem terkait
    
    @Column(name = "aspek_alur_kerja", columnDefinition = "TEXT")
    private String aspekAlurKerja; // Terhadap alur kerja bisnis
    
    @Column(name = "aspek_struktur_organisasi", columnDefinition = "TEXT")
    private String aspekStrukturOrganisasi; // Terhadap struktur organisasi

    // Terhadap dokumentasi - T.0.1
    @Column(name = "dok_t01_sebelum", columnDefinition = "TEXT")
    private String dokT01Sebelum;
    
    @Column(name = "dok_t01_sesudah", columnDefinition = "TEXT")
    private String dokT01Sesudah;

    // Terhadap dokumentasi - T.1.1
    @Column(name = "dok_t11_sebelum", columnDefinition = "TEXT")
    private String dokT11Sebelum;
    
    @Column(name = "dok_t11_sesudah", columnDefinition = "TEXT")
    private String dokT11Sesudah;

    // Terhadap Penggunaan Sistem - Jumlah Pengguna
    @Column(name = "pengguna_sebelum", length = 255)
    private String penggunaSebelum;
    
    @Column(name = "pengguna_sesudah", length = 255)
    private String penggunaSesudah;

    // Terhadap Penggunaan Sistem - Jumlah akses bersamaan
    @Column(name = "akses_bersamaan_sebelum", length = 255)
    private String aksesBersamaanSebelum;
    
    @Column(name = "akses_bersamaan_sesudah", length = 255)
    private String aksesBersamaanSesudah;

    // Terhadap Penggunaan Sistem - Pertumbuhan data
    @Column(name = "pertumbuhan_data_sebelum", length = 255)
    private String pertumbuhanDataSebelum;
    
    @Column(name = "pertumbuhan_data_sesudah", length = 255)
    private String pertumbuhanDataSesudah;

    // Jadwal Pelaksanaan
    @Column(name = "target_pengujian")
    private LocalDate targetPengujian;
    
    @Column(name = "target_deployment")
    private LocalDate targetDeployment;
    
    @Column(name = "target_go_live")
    private LocalDate targetGoLive;

    // Pernyataan (2 checkboxes)
    @Column(name = "pernyataan_1")
    @Builder.Default
    private Boolean pernyataan1 = false; // bersedia menerima konsekuensi...
    
    @Column(name = "pernyataan_2")
    @Builder.Default
    private Boolean pernyataan2 = false; // satker terdampak telah menyetujui...

    // === Fields for F.S.2 Disetujui ===
    
    // Progres: ASESMEN, CODING, PDKK, DEPLOY_SELESAI
    @Column(name = "progres", length = 50)
    private String progres;

    // Fase Pengajuan: DESAIN, PEMELIHARAAN
    @Column(name = "fase_pengajuan", length = 50)
    private String fasePengajuan;

    // IKU: Y or T
    @Column(name = "iku", length = 1)
    private String iku;

    // Mekanisme: INHOUSE, OUTSOURCE
    @Column(name = "mekanisme", length = 50)
    private String mekanisme;

    // Pelaksanaan: SINGLE_YEAR, MULTIYEARS
    @Column(name = "pelaksanaan", length = 50)
    private String pelaksanaan;

    // Tahun (for Single Year)
    @Column(name = "tahun")
    private Integer tahun;

    // Tahun Mulai (for Multiyears)
    @Column(name = "tahun_mulai")
    private Integer tahunMulai;

    // Tahun Selesai (for Multiyears)
    @Column(name = "tahun_selesai")
    private Integer tahunSelesai;

    // PIC (UUID reference to user)
    @Column(name = "pic_id")
    private UUID picId;

    @Column(name = "pic_name", length = 255)
    private String picName;

    // Team Structure (similar to PKSI)
    @Column(name = "anggota_tim", columnDefinition = "NVARCHAR(MAX)")
    private String anggotaTim;

    @Column(name = "anggota_tim_names", columnDefinition = "NVARCHAR(MAX)")
    private String anggotaTimNames;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private MstTeam team;

    // Dokumen Pengajuan F.S.2 reference (file ID or path)
    @Column(name = "dokumen_path", length = 500)
    private String dokumenPath;

    // === New Monitoring Fields ===
    
    // Dokumen Pengajuan F.S.2
    @Column(name = "nomor_nd", length = 100)
    private String nomorNd;

    @Column(name = "tanggal_nd")
    private LocalDate tanggalNd;

    @Column(name = "berkas_nd", length = 500)
    private String berkasNd;

    @Column(name = "berkas_fs2", length = 500)
    private String berkasFs2;

    // CD Prinsip
    @Column(name = "nomor_cd", length = 100)
    private String nomorCd;

    @Column(name = "tanggal_cd")
    private LocalDate tanggalCd;

    @Column(name = "berkas_cd", length = 500)
    private String berkasCd;

    @Column(name = "berkas_fs2a", length = 500)
    private String berkasFs2a;

    @Column(name = "berkas_fs2b", length = 500)
    private String berkasFs2b;

    // Pengujian
    @Column(name = "realisasi_pengujian")
    private LocalDate realisasiPengujian;

    @Column(name = "berkas_f45", length = 500)
    private String berkasF45;

    @Column(name = "berkas_f46", length = 500)
    private String berkasF46;

    // Deployment
    @Column(name = "realisasi_deployment")
    private LocalDate realisasiDeployment;

    @Column(name = "berkas_nd_ba_deployment", length = 500)
    private String berkasNdBaDeployment;

    // Keterangan
    @Column(name = "keterangan", columnDefinition = "TEXT")
    private String keterangan;
}
