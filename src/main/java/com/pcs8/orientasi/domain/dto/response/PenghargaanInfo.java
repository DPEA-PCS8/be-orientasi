package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenghargaanInfo {
    private UUID id;
    
    @JsonProperty("kategori")
    private VariableInfo kategori;
    
    @JsonProperty("tanggal")
    private LocalDate tanggal;
    
    @JsonProperty("deskripsi")
    private String deskripsi;
}
