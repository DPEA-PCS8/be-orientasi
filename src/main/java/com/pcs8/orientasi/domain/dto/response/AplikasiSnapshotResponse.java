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

import static com.pcs8.orientasi.domain.constants.AplikasiFieldNames.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AplikasiSnapshotResponse {

    private UUID id;

    @JsonProperty(APLIKASI_ID)
    private UUID aplikasiId;

    @JsonProperty(TAHUN)
    private Integer tahun;

    @JsonProperty(KODE_APLIKASI)
    private String kodeAplikasi;

    @JsonProperty(NAMA_APLIKASI)
    private String namaAplikasi;

    @JsonProperty(DESKRIPSI)
    private String deskripsi;

    @JsonProperty(STATUS_APLIKASI)
    private String statusAplikasi;

    @JsonProperty(TANGGAL_STATUS)
    private LocalDate tanggalStatus;

    @JsonProperty(BIDANG)
    private BidangInfo bidang;

    @JsonProperty(SKPA)
    private SkpaInfo skpa;

    @JsonProperty(TANGGAL_IMPLEMENTASI)
    private LocalDate tanggalImplementasi;

    @JsonProperty(AKSES)
    private String akses;

    @JsonProperty(PROSES_DATA_PRIBADI)
    private Boolean prosesDataPribadi;

    @JsonProperty(DATA_PRIBADI_DIPROSES)
    private String dataPribadiDiproses;

    @JsonProperty(IDLE_INFO)
    private IdleInfo idleInfo;

    // Nested lists
    @JsonProperty(URLS)
    private List<UrlInfo> urls;

    @JsonProperty(SATKER_INTERNALS)
    private List<SatkerInternalInfo> satkerInternals;

    @JsonProperty(PENGGUNA_EKSTERNALS)
    private List<PenggunaEksternalInfo> penggunaEksternals;

    @JsonProperty(KOMUNIKASI_SISTEMS)
    private List<KomunikasiSistemInfo> komunikasiSistems;

    @JsonProperty(PENGHARGAANS)
    private List<PenghargaanInfo> penghargaans;

    @JsonProperty(CHANGELOGS)
    private List<ChangelogInfo> changelogs;

    @JsonProperty(SNAPSHOT_DATE)
    private LocalDateTime snapshotDate;

    @JsonProperty(SNAPSHOT_TYPE)
    private String snapshotType;

    @JsonProperty(CREATED_AT)
    private LocalDateTime createdAt;

    @JsonProperty(UPDATED_AT)
    private LocalDateTime updatedAt;
}
