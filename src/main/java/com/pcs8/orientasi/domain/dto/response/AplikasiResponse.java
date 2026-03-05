package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AplikasiResponse {

    private UUID id;

    @JsonProperty("kode_aplikasi")
    private String kodeAplikasi;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    @JsonProperty("deskripsi")
    private String deskripsi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    @JsonProperty("bidang")
    private BidangInfo bidang;

    @JsonProperty("skpa")
    private SkpaInfo skpa;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("akses")
    private String akses;

    @JsonProperty("proses_data_pribadi")
    private Boolean prosesDataPribadi;

    @JsonProperty("data_pribadi_diproses")
    private String dataPribadiDiproses;

    // Idle-specific fields
    @JsonProperty("kategori_idle")
    private String kategoriIdle;

    @JsonProperty("alasan_idle")
    private String alasanIdle;

    @JsonProperty("rencana_pengakhiran")
    private String rencanaPengakhiran;

    @JsonProperty("alasan_belum_diakhiri")
    private String alasanBelumDiakhiri;

    // Nested lists
    @JsonProperty("urls")
    private List<UrlInfo> urls;

    @JsonProperty("satker_internals")
    private List<SatkerInternalInfo> satkerInternals;

    @JsonProperty("pengguna_eksternals")
    private List<PenggunaEksternalInfo> penggunaEksternals;

    @JsonProperty("komunikasi_sistems")
    private List<KomunikasiSistemInfo> komunikasiSistems;

    @JsonProperty("penghargaans")
    private List<PenghargaanInfo> penghargaans;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidangInfo {
        private UUID id;
        @JsonProperty("kode_bidang")
        private String kodeBidang;
        @JsonProperty("nama_bidang")
        private String namaBidang;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkpaInfo {
        private UUID id;
        @JsonProperty("kode_skpa")
        private String kodeSkpa;
        @JsonProperty("nama_skpa")
        private String namaSkpa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlInfo {
        private UUID id;
        private String url;
        @JsonProperty("tipe_akses")
        private String tipeAkses;
        private String keterangan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SatkerInternalInfo {
        private UUID id;
        @JsonProperty("nama_satker")
        private String namaSatker;
        private String keterangan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PenggunaEksternalInfo {
        private UUID id;
        @JsonProperty("nama_pengguna")
        private String namaPengguna;
        private String keterangan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KomunikasiSistemInfo {
        private UUID id;
        @JsonProperty("nama_sistem")
        private String namaSistem;
        @JsonProperty("tipe_sistem")
        private String tipeSistem;
        @JsonProperty("deskripsi_komunikasi")
        private String deskripsiKomunikasi;
        private String keterangan;
        @JsonProperty("is_planned")
        private Boolean isPlanned;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PenghargaanInfo {
        private UUID id;
        @JsonProperty("kategori")
        private VariableInfo kategori;
        @JsonProperty("tanggal")
        private LocalDate tanggal;
        @JsonProperty("deskripsi")
        private String deskripsi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableInfo {
        private UUID id;
        private String kode;
        private String nama;
    }
}
