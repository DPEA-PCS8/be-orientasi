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
public class SnapshotArsitekturRbsiResponse {

    private UUID id;

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("snapshot_date")
    private LocalDate snapshotDate;

    @JsonProperty("arsitektur_id")
    private UUID arsitekturId;

    @JsonProperty("sub_kategori_kode")
    private String subKategoriKode;

    @JsonProperty("sub_kategori_nama")
    private String subKategoriNama;

    @JsonProperty("aplikasi_kode")
    private String aplikasiKode;

    @JsonProperty("aplikasi_nama")
    private String aplikasiNama;

    @JsonProperty("aplikasi_baseline_kode")
    private String aplikasiBaselineKode;

    @JsonProperty("aplikasi_baseline_nama")
    private String aplikasiBaselineNama;

    @JsonProperty("aplikasi_target_kode")
    private String aplikasiTargetKode;

    @JsonProperty("aplikasi_target_nama")
    private String aplikasiTargetNama;

    @JsonProperty("action")
    private String action;

    @JsonProperty("year_statuses")
    private String yearStatuses;

    @JsonProperty("inisiatif_group_id")
    private UUID inisiatifGroupId;

    @JsonProperty("inisiatif_group_nama")
    private String inisiatifGroupNama;

    @JsonProperty("skpa_kode")
    private String skpaKode;

    @JsonProperty("skpa_nama")
    private String skpaNama;

    @JsonProperty("keterangan")
    private String keterangan;

    @JsonProperty("changes")
    private String changes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // --- Grouped snapshot summary per tanggal ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SnapshotGroup {

        @JsonProperty("snapshot_date")
        private LocalDate snapshotDate;

        @JsonProperty("total_items")
        private int totalItems;

        @JsonProperty("changed_items")
        private int changedItems;

        @JsonProperty("items")
        private List<SnapshotArsitekturRbsiResponse> items;
    }
}
