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
public class AplikasiRequest {

    @JsonProperty("kode_aplikasi")
    @NotBlank(message = "Kode aplikasi is required")
    @Size(max = 50, message = "Kode aplikasi max 50 characters")
    private String kodeAplikasi;

    @JsonProperty("nama_aplikasi")
    @NotBlank(message = "Nama aplikasi is required")
    @Size(max = 255, message = "Nama aplikasi max 255 characters")
    private String namaAplikasi;
}
