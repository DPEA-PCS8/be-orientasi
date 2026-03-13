package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fs2DocumentRequest {

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    @NotBlank(message = "Nama F.S.2 is required")
    @Size(max = 255, message = "Nama F.S.2 must not exceed 255 characters")
    @JsonProperty("nama_fs2")
    private String namaFs2;

    @JsonProperty("tanggal_pengajuan")
    private LocalDate tanggalPengajuan;

    @JsonProperty("bidang_id")
    private UUID bidangId;

    @JsonProperty("skpa_id")
    private UUID skpaId;

    // Status: PENDING, DISETUJUI, TIDAK_DISETUJUI
    private String status;

    // === Form Fields from requirement point 6 ===
    
    @JsonProperty("deskripsi_pengubahan")
    private String deskripsiPengubahan;

    @JsonProperty("alasan_pengubahan")
    private String alasanPengubahan;

    @JsonProperty("status_tahapan")
    private String statusTahapan; // DESAIN, PEMELIHARAAN

    private String urgensi; // RENDAH, SEDANG, TINGGI

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

    @JsonProperty("dokumen_path")
    private String dokumenPath;
}
