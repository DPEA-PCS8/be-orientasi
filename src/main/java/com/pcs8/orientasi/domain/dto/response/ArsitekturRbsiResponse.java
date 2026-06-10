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
public class ArsitekturRbsiResponse {

    private UUID id;

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("rbsi_periode")
    private String rbsiPeriode;

    @JsonProperty("sub_kategori")
    private SubKategoriResponse subKategori;

    // Aplikasi yang dipilih sebagai referensi utama
    @JsonProperty("aplikasi")
    private AplikasiResponse aplikasi;

    @JsonProperty("aplikasi_baseline")
    private String aplikasiBaseline;

    @JsonProperty("aplikasi_target")
    private String aplikasiTarget;

    @JsonProperty("action")
    private String action;

    @JsonProperty("year_statuses")
    private String yearStatuses;

    @JsonProperty("inisiatif_group")
    private InisiatifGroupSimpleResponse inisiatifGroup;

    @JsonProperty("skpa")
    private SkpaResponse skpa;

    @JsonProperty("keterangan")
    private String keterangan;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InisiatifGroupSimpleResponse {
        private UUID id;

        @JsonProperty("nama_inisiatif")
        private String namaInisiatif;

        @JsonProperty("keterangan")
        private String keterangan;
    }
}
