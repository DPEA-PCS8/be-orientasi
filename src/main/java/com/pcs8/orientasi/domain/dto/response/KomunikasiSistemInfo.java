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
public class KomunikasiSistemInfo {
    private UUID id;
    
    @JsonProperty("nama_sistem")
    private String namaSistem;
    
    @JsonProperty("tipe_sistem")
    private String tipeSistem;
    
    @JsonProperty("deskripsi_komunikasi")
    private String deskripsiKomunikasi;
    
    private String keterangan;
    
    @JsonProperty("is_planned")
    private Boolean isPlanned;
}
