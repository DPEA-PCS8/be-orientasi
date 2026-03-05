package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AplikasiRequest {

    @JsonProperty("kode_aplikasi")
    @NotBlank(message = "Kode aplikasi is required")
    @Size(max = 50, message = "Kode aplikasi max 50 characters")
    private String kodeAplikasi;

    @JsonProperty("nama_aplikasi")
    @NotBlank(message = "Nama aplikasi is required")
    @Size(max = 255, message = "Nama aplikasi max 255 characters")
    private String namaAplikasi;

    @JsonProperty("deskripsi")
    private String deskripsi;

    @JsonProperty("status_aplikasi")
    @NotBlank(message = "Status aplikasi is required")
    private String statusAplikasi; // AKTIF, IDLE, DIAKHIRI

    @JsonProperty("bidang_id")
    private UUID bidangId;

    @JsonProperty("skpa_id")
    private UUID skpaId;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("akses")
    private String akses; // INTERNET, INTRANET, BOTH

    @JsonProperty("proses_data_pribadi")
    @NotNull(message = "Proses data pribadi is required")
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

    // Nested objects
    @JsonProperty("urls")
    @Valid
    private List<UrlRequest> urls;

    @JsonProperty("satker_internals")
    @Valid
    private List<SatkerInternalRequest> satkerInternals;

    @JsonProperty("pengguna_eksternals")
    @Valid
    private List<PenggunaEksternalRequest> penggunaEksternals;

    @JsonProperty("komunikasi_sistems")
    @Valid
    private List<KomunikasiSistemRequest> komunikasiSistems;

    @JsonProperty("penghargaans")
    @Valid
    private List<PenghargaanRequest> penghargaans;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UrlRequest {
        @JsonProperty("url")
        @NotBlank(message = "URL is required")
        private String url;

        @JsonProperty("tipe_akses")
        private String tipeAkses;

        @JsonProperty("keterangan")
        private String keterangan;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SatkerInternalRequest {
        @JsonProperty("nama_satker")
        @NotBlank(message = "Nama satker is required")
        private String namaSatker;

        @JsonProperty("keterangan")
        private String keterangan;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PenggunaEksternalRequest {
        @JsonProperty("nama_pengguna")
        @NotBlank(message = "Nama pengguna is required")
        private String namaPengguna;

        @JsonProperty("keterangan")
        private String keterangan;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KomunikasiSistemRequest {
        @JsonProperty("nama_sistem")
        @NotBlank(message = "Nama sistem is required")
        private String namaSistem;

        @JsonProperty("tipe_sistem")
        private String tipeSistem;

        @JsonProperty("deskripsi_komunikasi")
        private String deskripsiKomunikasi;

        @JsonProperty("keterangan")
        private String keterangan;

        @JsonProperty("is_planned")
        private Boolean isPlanned;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PenghargaanRequest {
        @JsonProperty("kategori_id")
        @NotNull(message = "Kategori is required")
        private UUID kategoriId;

        @JsonProperty("tanggal")
        @NotNull(message = "Tanggal is required")
        private LocalDate tanggal;

        @JsonProperty("deskripsi")
        private String deskripsi;
    }
}
