package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangelogRequest {

    @JsonProperty("tanggal_perubahan")
    @NotNull(message = "Tanggal perubahan is required")
    private LocalDate tanggalPerubahan;

    @JsonProperty("keterangan")
    @NotBlank(message = "Keterangan is required")
    private String keterangan;
}
