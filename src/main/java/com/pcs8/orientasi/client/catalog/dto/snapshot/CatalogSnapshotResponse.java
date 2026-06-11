package com.pcs8.orientasi.client.catalog.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogSnapshotResponse {

    private UUID id;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    private String kategori;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

    // Raw JSON strings as stored in snapshot table
    private String urls;

    @JsonProperty("pengguna_internal")
    private String penggunaInternal;

    @JsonProperty("pengguna_eksternal")
    private String penggunaEksternal;

    @JsonProperty("penghargaan_aplikasi")
    private String penghargaanAplikasi;

    @JsonProperty("informasi_katalog")
    private String informasiKatalog;

    @JsonProperty("change_type")
    private String changeType;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
