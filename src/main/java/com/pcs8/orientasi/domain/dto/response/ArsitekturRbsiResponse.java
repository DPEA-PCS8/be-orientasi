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

    @JsonProperty("aplikasi_baseline")
    private AplikasiResponse aplikasiBaseline;

    @JsonProperty("aplikasi_target")
    private AplikasiResponse aplikasiTarget;

    @JsonProperty("action")
    private String action;

    @JsonProperty("year_statuses")
    private String yearStatuses;

    @JsonProperty("inisiatif")
    private InisiatifSimpleResponse inisiatif;

    @JsonProperty("skpa")
    private SkpaResponse skpa;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InisiatifSimpleResponse {
        private UUID id;

        @JsonProperty("nomor_inisiatif")
        private String nomorInisiatif;

        @JsonProperty("nama_inisiatif")
        private String namaInisiatif;

        @JsonProperty("program_id")
        private UUID programId;

        @JsonProperty("nama_program")
        private String namaProgram;
    }
}
