package com.pcs8.orientasi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class containing shared PKSI document fields.
 * Used by both Request and Response DTOs to reduce code duplication.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class PksiDocumentFields {

    @JsonProperty("aplikasi_id")
    protected String aplikasiId;

    @JsonProperty("nama_pksi")
    protected String namaPksi;

    @JsonProperty("tanggal_pengajuan")
    protected String tanggalPengajuan;

    @JsonProperty("deskripsi_pksi")
    protected String deskripsiPksi;

    @JsonProperty("mengapa_pksi_diperlukan")
    protected String mengapaPksiDiperlukan;

    @JsonProperty("kapan_harus_diselesaikan")
    protected String kapanHarusDiselesaikan;

    @JsonProperty("pic_satker_ba")
    protected String picSatkerBA;

    @JsonProperty("kegunaan_pksi")
    protected String kegunaanPksi;

    @JsonProperty("tujuan_pksi")
    protected String tujuanPksi;

    @JsonProperty("target_pksi")
    protected String targetPksi;

    @JsonProperty("ruang_lingkup")
    protected String ruangLingkup;

    @JsonProperty("batasan_pksi")
    protected String batasanPksi;

    @JsonProperty("hubungan_sistem_lain")
    protected String hubunganSistemLain;

    @JsonProperty("asumsi")
    protected String asumsi;

    @JsonProperty("batasan_desain")
    protected String batasanDesain;

    @JsonProperty("risiko_bisnis")
    protected String risikoBisnis;

    @JsonProperty("risiko_sukses_pksi")
    protected String risikoSuksesPksi;

    @JsonProperty("pengendalian_risiko")
    protected String pengendalianRisiko;

    @JsonProperty("pengelola_aplikasi")
    protected String pengelolaAplikasi;

    @JsonProperty("pengguna_aplikasi")
    protected String penggunaAplikasi;

    @JsonProperty("program_inisiatif_rbsi")
    protected String programInisiatifRbsi;

    @JsonProperty("fungsi_aplikasi")
    protected String fungsiAplikasi;

    @JsonProperty("informasi_yang_dikelola")
    protected String informasiYangDikelola;

    @JsonProperty("dasar_peraturan")
    protected String dasarPeraturan;

    @JsonProperty("tahap1_awal")
    protected String tahap1Awal;

    @JsonProperty("tahap1_akhir")
    protected String tahap1Akhir;

    @JsonProperty("tahap5_awal")
    protected String tahap5Awal;

    @JsonProperty("tahap5_akhir")
    protected String tahap5Akhir;

    @JsonProperty("tahap7_awal")
    protected String tahap7Awal;

    @JsonProperty("tahap7_akhir")
    protected String tahap7Akhir;

    @JsonProperty("rencana_pengelolaan")
    protected String rencanaPengelolaan;
}
