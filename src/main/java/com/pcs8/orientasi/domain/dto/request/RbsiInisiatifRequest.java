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
public class RbsiInisiatifRequest {

    @JsonProperty("program_id")
    @NotNull(message = "Program id is required")
    private UUID programId;

    @JsonProperty("tahun")
    @NotNull(message = "Tahun is required")
    private Integer tahun;

    @JsonProperty("nomor_inisiatif")
    @NotBlank(message = "Nomor inisiatif is required")
    private String nomorInisiatif;

    @JsonProperty("nama_inisiatif")
    @NotBlank(message = "Nama inisiatif is required")
    private String namaInisiatif;
}
