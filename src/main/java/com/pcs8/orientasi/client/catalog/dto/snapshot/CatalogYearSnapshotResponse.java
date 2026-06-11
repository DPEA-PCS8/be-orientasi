package com.pcs8.orientasi.client.catalog.dto.snapshot;

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
public class CatalogYearSnapshotResponse {

    private UUID id;

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    private Integer tahun;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;

    private String deskripsi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

    private String bidang;
    private String skpa;
    private String kategori;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("proses_data_pribadi")
    private Boolean prosesDataPribadi;

    @JsonProperty("data_pribadi_diproses")
    private String dataPribadiDiproses;

    @JsonProperty("idle_info")
    private CatalogIdleInfo idleInfo;

    // Raw JSON strings — parsed in service layer via ObjectMapper
    private String urls;

    @JsonProperty("pengguna_internal")
    private String penggunaInternal;

    @JsonProperty("pengguna_eksternal")
    private String penggunaEksternal;

    @JsonProperty("penghargaan_aplikasi")
    private String penghargaanAplikasi;

    @JsonProperty("informasi_katalog")
    private String informasiKatalog;

    @JsonProperty("snapshot_type")
    private String snapshotType;

    @JsonProperty("snapshot_date")
    private LocalDateTime snapshotDate;

    private List<ChangelogItem> changelogs;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChangelogItem {
        private UUID id;

        @JsonProperty("tanggal_perubahan")
        private LocalDate tanggalPerubahan;

        private String keterangan;

        @JsonProperty("perubahan_detail")
        private String perubahanDetail;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }
}
