package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RbsiKepRequest {

    @JsonProperty("nomor_kep")
    @NotBlank(message = "Nomor KEP is required")
    private String nomorKep;

    @JsonProperty("tahun_pelaporan")
    @NotNull(message = "Tahun pelaporan is required")
    private Integer tahunPelaporan;

    @JsonProperty("copy_from_latest")
    private Boolean copyFromLatest;
}
