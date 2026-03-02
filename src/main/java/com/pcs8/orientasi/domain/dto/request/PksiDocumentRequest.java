package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MVP Request untuk dokumen T.01 (PKSI)
 * Field minimal yang harus diisi saat pengajuan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PksiDocumentRequest {

    // ==================== HEADER ====================
    @JsonProperty("nama_pksi")
    @NotBlank(message = "Nama PKSI is required")
    private String namaPksi;

    // ==================== SECTION 1: PENDAHULUAN ====================
    @JsonProperty("deskripsi_pksi")
    @NotBlank(message = "Deskripsi PKSI is required")
    private String deskripsiPksi;

    @JsonProperty("tujuan_pengajuan")
    @NotBlank(message = "Tujuan pengajuan is required")
    private String tujuanPengajuan;

    @JsonProperty("kapan_diselesaikan")
    @NotBlank(message = "Target penyelesaian is required")
    private String kapanDiselesaikan;

    @JsonProperty("pic_satker")
    @NotBlank(message = "PIC Satker is required")
    private String picSatker;

    // ==================== SECTION 2: TUJUAN ====================
    @JsonProperty("tujuan_pksi")
    @NotBlank(message = "Tujuan PKSI is required")
    private String tujuanPksi;

    // ==================== SECTION 3: CAKUPAN ====================
    @JsonProperty("ruang_lingkup")
    @NotBlank(message = "Ruang lingkup is required")
    private String ruangLingkup;

    // ==================== SECTION 5: GAMBARAN APLIKASI ====================
    @JsonProperty("pengelola_aplikasi")
    @NotBlank(message = "Pengelola aplikasi is required")
    private String pengelolaAplikasi;

    @JsonProperty("pengguna_aplikasi")
    @NotBlank(message = "Pengguna aplikasi is required")
    private String penggunaAplikasi;

    @JsonProperty("program_inisiatif_rbsi")
    @NotBlank(message = "Program inisiatif RBSI is required")
    private String programInisiatifRbsi;

    @JsonProperty("fungsi_aplikasi")
    @NotBlank(message = "Fungsi aplikasi is required")
    private String fungsiAplikasi;
}
