package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSnapshotRequest {

    @JsonProperty("kode_aplikasi")
    @Size(max = 50, message = "Kode aplikasi max 50 characters")
    private String kodeAplikasi;

    @JsonProperty("nama_aplikasi")
    @Size(max = 255, message = "Nama aplikasi max 255 characters")
    private String namaAplikasi;

    @JsonProperty("deskripsi")
    private String deskripsi;

    @JsonProperty("status_aplikasi")
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
    private Boolean prosesDataPribadi;

    @JsonProperty("keterangan_historis")
    private String keteranganHistoris;
}
