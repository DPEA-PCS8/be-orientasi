package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response untuk dokumen T.01 (PKSI) - Full Version
 * Mendukung semua field dari form frontend
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PksiDocumentResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    // ==================== HEADER ====================
    @JsonProperty("nama_pksi")
    private String namaPksi;

    @JsonProperty("tanggal_pengajuan")
    private String tanggalPengajuan;

    // ==================== SECTION 1: PENDAHULUAN ====================
    @JsonProperty("deskripsi_pksi")
    private String deskripsiPksi;

    @JsonProperty("mengapa_pksi_diperlukan")
    private String mengapaPksiDiperlukan;

    @JsonProperty("kapan_harus_diselesaikan")
    private String kapanHarusDiselesaikan;

    @JsonProperty("pic_satker_ba")
    private String picSatkerBA;

    // ==================== SECTION 2: TUJUAN DAN KEGUNAAN ====================
    @JsonProperty("kegunaan_pksi")
    private String kegunaanPksi;

    @JsonProperty("tujuan_pksi")
    private String tujuanPksi;

    @JsonProperty("target_pksi")
    private String targetPksi;

    // ==================== SECTION 3: CAKUPAN ====================
    @JsonProperty("ruang_lingkup")
    private String ruangLingkup;

    @JsonProperty("batasan_pksi")
    private String batasanPksi;

    @JsonProperty("hubungan_sistem_lain")
    private String hubunganSistemLain;

    @JsonProperty("asumsi")
    private String asumsi;

    // ==================== SECTION 4: RISIKO DAN BATASAN ====================
    @JsonProperty("batasan_desain")
    private String batasanDesain;

    @JsonProperty("risiko_bisnis")
    private String risikoBisnis;

    @JsonProperty("risiko_sukses_pksi")
    private String risikoSuksesPksi;

    @JsonProperty("pengendalian_risiko")
    private String pengendalianRisiko;

    // ==================== SECTION 5: GAMBARAN UMUM APLIKASI ====================
    @JsonProperty("pengelola_aplikasi")
    private String pengelolaAplikasi;

    @JsonProperty("pengguna_aplikasi")
    private String penggunaAplikasi;

    @JsonProperty("program_inisiatif_rbsi")
    private String programInisiatifRbsi;

    @JsonProperty("fungsi_aplikasi")
    private String fungsiAplikasi;

    @JsonProperty("informasi_yang_dikelola")
    private String informasiYangDikelola;

    @JsonProperty("dasar_peraturan")
    private String dasarPeraturan;

    // ==================== SECTION 6: USULAN JADWAL PELAKSANAAN ====================
    @JsonProperty("tahap1_awal")
    private String tahap1Awal;

    @JsonProperty("tahap1_akhir")
    private String tahap1Akhir;

    @JsonProperty("tahap5_awal")
    private String tahap5Awal;

    @JsonProperty("tahap5_akhir")
    private String tahap5Akhir;

    @JsonProperty("tahap7_awal")
    private String tahap7Awal;

    @JsonProperty("tahap7_akhir")
    private String tahap7Akhir;

    // ==================== SECTION 7: RENCANA PENGELOLAAN ====================
    @JsonProperty("rencana_pengelolaan")
    private String rencanaPengelolaan;

    // ==================== STATUS & METADATA ====================
    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    // ==================== LEGACY FIELDS (backward compatibility) ====================
    @JsonProperty("tujuan_pengajuan")
    private String tujuanPengajuan;

    @JsonProperty("kapan_diselesaikan")
    private String kapanDiselesaikan;

    @JsonProperty("pic_satker")
    private String picSatker;
}
