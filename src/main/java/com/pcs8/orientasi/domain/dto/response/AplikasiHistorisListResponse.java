package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AplikasiHistorisListResponse {

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    @JsonProperty("kode_aplikasi")
    private String kodeAplikasi;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    @JsonProperty("bidang_kode")
    private String bidangKode;

    @JsonProperty("bidang_nama")
    private String bidangNama;

    @JsonProperty("skpa_kode")
    private String skpaKode;

    @JsonProperty("skpa_nama")
    private String skpaNama;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    @JsonProperty("keterangan_historis")
    private String keteranganHistoris;

    @JsonProperty("tahun")
    private Integer tahun;

    @JsonProperty("snapshot_date")
    private LocalDateTime snapshotDate;
}
