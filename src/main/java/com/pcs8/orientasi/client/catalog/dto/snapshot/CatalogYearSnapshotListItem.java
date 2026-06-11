package com.pcs8.orientasi.client.catalog.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogYearSnapshotListItem {

    private UUID id;

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    private Integer tahun;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

    private String bidang;
    private String skpa;

    @JsonProperty("snapshot_type")
    private String snapshotType;

    @JsonProperty("snapshot_date")
    private LocalDateTime snapshotDate;
}
