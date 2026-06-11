package com.pcs8.orientasi.client.catalog.dto.aplikasi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogIdleInfo;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogAplikasiResponse {

    private UUID id;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;

    private String deskripsi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    private String bidang;
    private String skpa;
    private String kategori;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("proses_data_pribadi")
    private Boolean prosesDataPribadi;

    @JsonProperty("data_pribadi_diproses")
    private String dataPribadiDiproses;

    @JsonProperty("idle_info")
    private CatalogIdleInfo idleInfo;

    private List<UrlInfo> urls;

    @JsonProperty("pengguna_internal")
    private List<PenggunaInternalInfo> penggunaInternal;

    @JsonProperty("pengguna_eksternal")
    private List<PenggunaEksternalInfo> penggunaEksternal;

    @JsonProperty("penghargaan_aplikasi")
    private List<PenghargaanInfo> penghargaanAplikasi;

    @JsonProperty("informasi_katalog")
    private List<InformasiKatalogInfo> informasiKatalog;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UrlInfo {
        private UUID id;
        private String url;

        @JsonProperty("tipe_akses")
        private String tipeAkses;

        private String keterangan;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PenggunaInternalInfo {
        private UUID id;

        @JsonProperty("nama_satker")
        private String namaSatker;

        private String keterangan;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PenggunaEksternalInfo {
        private UUID id;

        @JsonProperty("nama_pengguna")
        private String namaPengguna;

        private String keterangan;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PenghargaanInfo {
        private UUID id;
        private String deskripsi;
        private LocalDate tanggal;
        private String kategori;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InformasiKatalogInfo {
        private UUID id;

        @JsonProperty("nama_informasi")
        private String namaInformasi;

        private String deskripsi;
    }
}
