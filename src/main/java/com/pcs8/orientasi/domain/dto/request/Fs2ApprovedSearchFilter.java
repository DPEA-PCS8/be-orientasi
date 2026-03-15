package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Filter DTO for searching approved F.S.2 documents.
 * Used to reduce the number of method parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fs2ApprovedSearchFilter {

    private String search;

    @JsonProperty("bidang_id")
    private UUID bidangId;

    @JsonProperty("skpa_id")
    private UUID skpaId;

    private String progres;

    @JsonProperty("fase_pengajuan")
    private String fasePengajuan;

    private String mekanisme;

    private String pelaksanaan;
}
