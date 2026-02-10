package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RbsiInisiatifItemRequest {

    @JsonProperty("nomor_inisiatif")
    @NotBlank(message = "Nomor inisiatif is required")
    private String nomorInisiatif;

    @JsonProperty("nama_inisiatif")
    @NotBlank(message = "Nama inisiatif is required")
    private String namaInisiatif;

    @JsonProperty("pksi_id")
    private Long pksiId;
}
