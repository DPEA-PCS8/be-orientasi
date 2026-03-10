package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KomunikasiSistemRequest {
    @JsonProperty("nama_sistem")
    @NotBlank(message = "Nama sistem is required")
    private String namaSistem;

    @JsonProperty("tipe_sistem")
    private String tipeSistem;

    @JsonProperty("deskripsi_komunikasi")
    private String deskripsiKomunikasi;

    @JsonProperty("keterangan")
    private String keterangan;

    @JsonProperty("is_planned")
    private Boolean isPlanned;
}
