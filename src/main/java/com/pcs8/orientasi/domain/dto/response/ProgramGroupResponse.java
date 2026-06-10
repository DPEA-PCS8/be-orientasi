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
public class ProgramGroupResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("nama_program")
    private String namaProgram;

    @JsonProperty("keterangan")
    private String keterangan;

    // TODO: review — decide whether to include tahun_list + nomor_program_by_year here
    //       (same as InisiatifGroupResponse.tahunList / nomorInisiatifByYear).
    //       Useful for the FE to know which years this program group spans.
    @JsonProperty("tahun_list")
    private List<Integer> tahunList;

    @JsonProperty("nomor_program_by_year")
    private List<YearNomor> nomorProgramByYear;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearNomor {
        @JsonProperty("tahun")
        private Integer tahun;

        @JsonProperty("nomor_program")
        private String nomorProgram;
    }
}
