package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkpaRequest {

    @JsonProperty("kode_skpa")
    @NotBlank(message = "Kode SKPA is required")
    @Size(max = 50, message = "Kode SKPA max 50 characters")
    private String kodeSkpa;

    @JsonProperty("nama_skpa")
    @NotBlank(message = "Nama SKPA is required")
    @Size(max = 255, message = "Nama SKPA max 255 characters")
    private String namaSkpa;

    @JsonProperty("keterangan")
    @Size(max = 255, message = "Keterangan max 255 characters")
    private String keterangan;

    @JsonProperty("bidang_id")
    private UUID bidangId;
}

