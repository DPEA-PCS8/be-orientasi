package com.pcs8.orientasi.client.catalog.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogSnapshotUpdateRequest {

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    private String kepanjangan;

    private String deskripsi;

    @JsonProperty("status_aplikasi")
    private String statusAplikasi;

    private String bidang;
    private String skpa;

    @JsonProperty("tanggal_implementasi")
    private LocalDate tanggalImplementasi;

    @JsonProperty("proses_data_pribadi")
    private Boolean prosesDataPribadi;

    @JsonProperty("keterangan_historis")
    private String keteranganHistoris;

    @JsonProperty("idle_info")
    private IdleInfoRequest idleInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IdleInfoRequest {
        @JsonProperty("kategori_idle")
        private String kategoriIdle;

        @JsonProperty("alasan_idle")
        private String alasanIdle;

        @JsonProperty("rencana_pengakhiran")
        private String rencanaPengakhiran;

        @JsonProperty("alasan_belum_diakhiri")
        private String alasanBelumDiakhiri;
    }
}
