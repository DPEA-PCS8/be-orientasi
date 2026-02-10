package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RbsiProgramResponse {

    private Long id;

    @JsonProperty("rbsi_id")
    private Long rbsiId;

    @JsonProperty("tahun")
    private Integer tahun;

    @JsonProperty("nomor_program")
    private String nomorProgram;

    @JsonProperty("nama_program")
    private String namaProgram;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("inisiatifs")
    private List<RbsiInisiatifResponse> inisiatifs;
}
