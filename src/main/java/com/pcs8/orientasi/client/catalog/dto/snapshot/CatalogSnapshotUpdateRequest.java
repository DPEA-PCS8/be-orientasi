package com.pcs8.orientasi.client.catalog.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogIdleInfoRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogSnapshotUpdateRequest {

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;

    private String deskripsi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    private String bidang;
    private String skpa;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("proses_data_pribadi")
    private Boolean prosesDataPribadi;

    @JsonProperty("keterangan_historis")
    private String keteranganHistoris;

    @JsonProperty("idle_info")
    private CatalogIdleInfoRequest idleInfo;
}
