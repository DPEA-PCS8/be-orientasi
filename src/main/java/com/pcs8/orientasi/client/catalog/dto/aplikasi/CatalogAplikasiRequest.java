package com.pcs8.orientasi.client.catalog.dto.aplikasi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogAplikasiRequest {

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;
    private String deskripsi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    private String skpa;
    private String bidang;
    private String kategori;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("proses_data_pribadi")
    private Boolean prosesDataPribadi;

    @JsonProperty("data_pribadi_diproses")
    private String dataPribadiDiproses;

    private List<UrlRequest> urls;

    @JsonProperty("pengguna_internal")
    private List<PenggunaInternalRequest> penggunaInternal;

    @JsonProperty("pengguna_eksternal")
    private List<PenggunaEksternalRequest> penggunaEksternal;

    @JsonProperty("penghargaan_aplikasi")
    private List<PenghargaanRequest> penghargaanAplikasi;

    @JsonProperty("informasi_katalog")
    private List<InformasiKatalogRequest> informasiKatalog;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UrlRequest {
        private String url;

        @JsonProperty("tipe_akses")
        private String tipeAkses;

        private String keterangan;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PenggunaInternalRequest {
        @JsonProperty("nama_satker")
        private String namaSatker;

        private String keterangan;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PenggunaEksternalRequest {
        @JsonProperty("nama_pengguna")
        private String namaPengguna;

        private String keterangan;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PenghargaanRequest {
        private String deskripsi;
        private LocalDate tanggal;
        private String kategori;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InformasiKatalogRequest {
        @JsonProperty("nama_informasi")
        private String namaInformasi;

        private String deskripsi;
    }
}
