package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PksiHistorisResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("nama_pksi")
    private String namaPksi;

    @JsonProperty("tahun")
    private String tahun;

    @JsonProperty("ruang_lingkup")
    private String ruangLingkup;

    @JsonProperty("status")
    private String status;
}
