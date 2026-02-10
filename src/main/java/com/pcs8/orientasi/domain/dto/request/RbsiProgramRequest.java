package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RbsiProgramRequest {

    @JsonProperty("rbsi_id")
    @NotNull(message = "Rbsi id is required")
    private Long rbsiId;

    @JsonProperty("tahun")
    @NotNull(message = "Tahun is required")
    private Integer tahun;

    @JsonProperty("nomor_program")
    @NotBlank(message = "Nomor program is required")
    private String nomorProgram;

    @JsonProperty("nama_program")
    @NotBlank(message = "Nama program is required")
    private String namaProgram;

    @JsonProperty("inisiatifs")
    @Valid
    private List<RbsiInisiatifItemRequest> inisiatifs;
}
