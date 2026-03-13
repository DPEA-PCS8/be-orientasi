package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InisiatifGroupResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("nama_inisiatif")
    private String namaInisiatif;

    @JsonProperty("keterangan")
    private String keterangan;

    @JsonProperty("tahun_list")
    private List<Integer> tahunList; // Years where this initiative exists

    @JsonProperty("nomor_inisiatif_by_year")
    private List<YearNomor> nomorInisiatifByYear; // Nomor per year

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearNomor {
        @JsonProperty("tahun")
        private Integer tahun;

        @JsonProperty("nomor_inisiatif")
        private String nomorInisiatif;

        @JsonProperty("program_nomor")
        private String programNomor;
    }
}
