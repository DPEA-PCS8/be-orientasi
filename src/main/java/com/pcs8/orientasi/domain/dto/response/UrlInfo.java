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
public class UrlInfo {
    private UUID id;
    private String url;
    
    @JsonProperty("tipe_akses")
    private String tipeAkses;
    
    private String keterangan;
}
