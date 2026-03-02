package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response untuk dokumen T.01 (PKSI) - MVP Version
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

    // Header
    @JsonProperty("nama_pksi")
    private String namaPksi;

    // Section 1 - Pendahuluan
    @JsonProperty("deskripsi_pksi")
    private String deskripsiPksi;

    @JsonProperty("tujuan_pengajuan")
    private String tujuanPengajuan;

    @JsonProperty("kapan_diselesaikan")
    private String kapanDiselesaikan;

    @JsonProperty("pic_satker")
    private String picSatker;

    // Section 2 - Tujuan
    @JsonProperty("tujuan_pksi")
    private String tujuanPksi;

    // Section 3 - Cakupan
    @JsonProperty("ruang_lingkup")
    private String ruangLingkup;

    // Section 5 - Gambaran Aplikasi
    @JsonProperty("pengelola_aplikasi")
    private String pengelolaAplikasi;

    @JsonProperty("pengguna_aplikasi")
    private String penggunaAplikasi;

    @JsonProperty("program_inisiatif_rbsi")
    private String programInisiatifRbsi;

    @JsonProperty("fungsi_aplikasi")
    private String fungsiAplikasi;

    // Status & Metadata
    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
