package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiInisiatifResponse {

    private Long id;

    @JsonProperty("program_id")
    private Long programId;

    @JsonProperty("tahun")
    private Integer tahun;

    @JsonProperty("nomor_inisiatif")
    private String nomorInisiatif;

    @JsonProperty("nama_inisiatif")
    private String namaInisiatif;

    @JsonProperty("pksi_id")
    private Long pksiId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
