package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiInisiatifResponse {

    private UUID id;

    @JsonProperty("program_id")
    private UUID programId;

    @JsonProperty("tahun")
    private Integer tahun;

    @JsonProperty("nomor_inisiatif")
    private String nomorInisiatif;

    @JsonProperty("nama_inisiatif")
    private String namaInisiatif;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
