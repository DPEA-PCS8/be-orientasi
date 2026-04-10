package com.pcs8.orientasi.domain.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Base class containing shared form fields between Fs2DocumentRequest and Fs2DocumentResponse.
 * This eliminates code duplication detected by SonarQube.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Fs2FormFieldsBase {

    // === Form Fields from requirement point 6 ===
    
    @JsonProperty("deskripsi_pengubahan")
    private String deskripsiPengubahan;

    @JsonProperty("alasan_pengubahan")
    private String alasanPengubahan;

    @JsonProperty("status_tahapan")
    private String statusTahapan;

    private String urgensi;

    // Kesesuaian Kriteria (4 checkboxes)
    @JsonProperty("kriteria_1")
    private Boolean kriteria1;

    @JsonProperty("kriteria_2")
    private Boolean kriteria2;

    @JsonProperty("kriteria_3")
    private Boolean kriteria3;

    @JsonProperty("kriteria_4")
    private Boolean kriteria4;

    // Aspek Perubahan
    @JsonProperty("aspek_sistem_ada")
    private String aspekSistemAda;

    @JsonProperty("aspek_sistem_terkait")
    private String aspekSistemTerkait;

    @JsonProperty("aspek_alur_kerja")
    private String aspekAlurKerja;

    @JsonProperty("aspek_struktur_organisasi")
    private String aspekStrukturOrganisasi;

    // Dokumentasi T.0.1
    @JsonProperty("dok_t01_sebelum")
    private String dokT01Sebelum;

    @JsonProperty("dok_t01_sesudah")
    private String dokT01Sesudah;

    // Dokumentasi T.1.1
    @JsonProperty("dok_t11_sebelum")
    private String dokT11Sebelum;

    @JsonProperty("dok_t11_sesudah")
    private String dokT11Sesudah;

    // Penggunaan Sistem
    @JsonProperty("pengguna_sebelum")
    private String penggunaSebelum;

    @JsonProperty("pengguna_sesudah")
    private String penggunaSesudah;

    @JsonProperty("akses_bersamaan_sebelum")
    private String aksesBersamaanSebelum;

    @JsonProperty("akses_bersamaan_sesudah")
    private String aksesBersamaanSesudah;

    @JsonProperty("pertumbuhan_data_sebelum")
    private String pertumbuhanDataSebelum;

    @JsonProperty("pertumbuhan_data_sesudah")
    private String pertumbuhanDataSesudah;

    // Jadwal Pelaksanaan
    @JsonProperty("target_pengujian")
    private LocalDate targetPengujian;

    @JsonProperty("target_deployment")
    private LocalDate targetDeployment;

    @JsonProperty("target_go_live")
    private LocalDate targetGoLive;

    // Pernyataan (2 checkboxes)
    @JsonProperty("pernyataan_1")
    private Boolean pernyataan1;

    @JsonProperty("pernyataan_2")
    private Boolean pernyataan2;

    // Fields for F.S.2 Disetujui
    private String progres;

    @JsonProperty("fase_pengajuan")
    private String fasePengajuan;

    private String iku;

    private String mekanisme;

    private String pelaksanaan;

    private Integer tahun;

    @JsonProperty("tahun_mulai")
    private Integer tahunMulai;

    @JsonProperty("tahun_selesai")
    private Integer tahunSelesai;

    @JsonProperty("pic_id")
    private UUID picId;

    // Team Structure
    @JsonProperty("team_id")
    private UUID teamId;

    @JsonProperty("anggota_tim")
    private String anggotaTim;

    @JsonProperty("anggota_tim_names")
    private String anggotaTimNames;

    @JsonProperty("dokumen_path")
    private String dokumenPath;

    // === New Monitoring Fields ===
    
    // Dokumen Pengajuan F.S.2
    @JsonProperty("nomor_nd")
    private String nomorNd;

    @JsonProperty("tanggal_nd")
    private LocalDate tanggalNd;

    @JsonProperty("berkas_nd")
    private String berkasNd;

    @JsonProperty("berkas_fs2")
    private String berkasFs2;

    // CD Prinsip
    @JsonProperty("nomor_cd")
    private String nomorCd;

    @JsonProperty("tanggal_cd")
    private LocalDate tanggalCd;

    @JsonProperty("berkas_cd")
    private String berkasCd;

    @JsonProperty("berkas_fs2a")
    private String berkasFs2a;

    @JsonProperty("berkas_fs2b")
    private String berkasFs2b;

    // Pengujian
    @JsonProperty("realisasi_pengujian")
    private LocalDate realisasiPengujian;

    @JsonProperty("berkas_f45")
    private String berkasF45;

    @JsonProperty("berkas_f46")
    private String berkasF46;

    // Deployment
    @JsonProperty("realisasi_deployment")
    private LocalDate realisasiDeployment;

    @JsonProperty("berkas_nd_ba_deployment")
    private String berkasNdBaDeployment;

    // Keterangan
    private String keterangan;
}
