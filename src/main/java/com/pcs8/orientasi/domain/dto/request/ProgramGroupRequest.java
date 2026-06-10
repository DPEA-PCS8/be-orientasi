package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramGroupRequest {

    @JsonProperty("rbsi_id")
    @NotNull(message = "Rbsi id is required")
    private UUID rbsiId;

    @JsonProperty("nama_program")
    @NotBlank(message = "Nama program is required")
    private String namaProgram;

    @JsonProperty("keterangan")
    private String keterangan;
}
