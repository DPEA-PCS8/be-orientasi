package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SatkerInternalRequest {
    @JsonProperty("nama_satker")
    @NotBlank(message = "Nama satker is required")
    private String namaSatker;

    @JsonProperty("keterangan")
    private String keterangan;
}
