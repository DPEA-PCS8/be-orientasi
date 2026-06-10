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
public class AplikasiListResponse {

    private UUID id;

    @JsonProperty("kode_aplikasi")
    private String kodeAplikasi;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    @JsonProperty("bidang")
    private BidangInfo bidang;

    @JsonProperty("skpa")
    private SkpaInfo skpa;

    @JsonProperty("sub_kategori")
    private SubKategoriInfo subKategori;
}
