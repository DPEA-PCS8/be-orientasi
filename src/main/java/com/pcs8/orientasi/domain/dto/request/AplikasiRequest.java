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

    @JsonProperty("sub_kategori_id")
    private UUID subKategoriId;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("akses")
    private String akses; // Comma-separated: INTERNET, INTRANET, EXTRANET, DESKTOP_APP, MOBILE_APP, or custom text

    @JsonProperty("proses_data_pribadi")
    @NotNull(message = "Proses data pribadi is required")
    private Boolean prosesDataPribadi;

    @JsonProperty("data_pribadi_diproses")
    private String dataPribadiDiproses;

    @JsonProperty("idle_info")
    @Valid
    private IdleRequest idleInfo;

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

    // Optional: Keterangan perubahan untuk historis
    @JsonProperty("keterangan_perubahan")
    private String keteranganPerubahan;
}
