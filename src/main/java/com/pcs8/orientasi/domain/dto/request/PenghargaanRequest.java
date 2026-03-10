package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PenghargaanRequest {
    @JsonProperty("kategori_id")
    @NotNull(message = "Kategori is required")
    private UUID kategoriId;

    @JsonProperty("tanggal")
    @NotNull(message = "Tanggal is required")
    private LocalDate tanggal;

    @JsonProperty("deskripsi")
    private String deskripsi;
}
