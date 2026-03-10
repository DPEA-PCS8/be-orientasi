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
public class AplikasiSnapshotResponse {

    private UUID id;

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    @JsonProperty("tahun")
    private Integer tahun;

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

    @JsonProperty("idle_info")
    private IdleInfo idleInfo;

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

    @JsonProperty("changelogs")
    private List<ChangelogInfo> changelogs;

    @JsonProperty("snapshot_date")
    private LocalDateTime snapshotDate;

    @JsonProperty("snapshot_type")
    private String snapshotType;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
