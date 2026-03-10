package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PenggunaEksternalRequest {
    @JsonProperty("nama_pengguna")
    @NotBlank(message = "Nama pengguna is required")
    private String namaPengguna;

    @JsonProperty("keterangan")
    private String keterangan;
}
