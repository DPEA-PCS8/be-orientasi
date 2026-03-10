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
public class SkpaInfo {
    private UUID id;
    
    @JsonProperty("kode_skpa")
    private String kodeSkpa;
    
    @JsonProperty("nama_skpa")
    private String namaSkpa;
}
