package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenggunaEksternalInfo {
    private UUID id;
    
    @JsonProperty("nama_pengguna")
    private String namaPengguna;
    
    private String keterangan;
}
