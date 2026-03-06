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

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

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
}
