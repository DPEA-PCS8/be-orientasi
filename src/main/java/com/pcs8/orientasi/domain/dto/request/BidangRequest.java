package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidangRequest {

    @JsonProperty("kode_bidang")
    @NotBlank(message = "Kode Bidang is required")
    @Size(max = 50, message = "Kode Bidang max 50 characters")
    private String kodeBidang;

    @JsonProperty("nama_bidang")
    @NotBlank(message = "Nama Bidang is required")
    @Size(max = 255, message = "Nama Bidang max 255 characters")
    private String namaBidang;
}
