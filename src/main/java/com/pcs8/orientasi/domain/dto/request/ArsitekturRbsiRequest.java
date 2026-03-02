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

    @JsonProperty("sub_kategori_id")
    @NotNull(message = "Sub Kategori ID is required")
    private UUID subKategoriId;

    @JsonProperty("aplikasi_baseline_id")
    private UUID aplikasiBaselineId;

    @JsonProperty("aplikasi_target_id")
    private UUID aplikasiTargetId;

    @JsonProperty("action")
    @Size(max = 50, message = "Action max 50 characters")
    private String action;

    @JsonProperty("year_statuses")
    @Size(max = 500, message = "Year statuses max 500 characters")
    private String yearStatuses;

    @JsonProperty("inisiatif_id")
    private UUID inisiatifId;

    @JsonProperty("skpa_id")
    private UUID skpaId;
}
