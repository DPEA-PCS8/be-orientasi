package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArsitekturRbsiRequest {

    @JsonProperty("rbsi_id")
    @NotNull(message = "RBSI ID is required")
    private UUID rbsiId;

    // Aplikasi yang dipilih user — menjadi sumber auto-fill sub_kategori & skpa
    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    // Auto-filled dari aplikasi, tapi editable oleh user
    @JsonProperty("sub_kategori_id")
    private UUID subKategoriId;

    @JsonProperty("skpa_id")
    private UUID skpaId;

    @JsonProperty("aplikasi_baseline")
    private String aplikasiBaseline;

    @JsonProperty("aplikasi_target")
    private String aplikasiTarget;

    @JsonProperty("action")
    @Size(max = 50, message = "Action max 50 characters")
    private String action;

    @JsonProperty("year_statuses")
    @Size(max = 500, message = "Year statuses max 500 characters")
    private String yearStatuses;

    // Relasi ke InisiatifGroup (bukan RbsiInisiatif)
    @JsonProperty("inisiatif_group_id")
    private UUID inisiatifGroupId;

    @JsonProperty("keterangan")
    private String keterangan;
}
