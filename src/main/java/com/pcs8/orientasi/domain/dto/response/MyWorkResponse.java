package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyWorkResponse {

    private List<TeamSummary> teams;

    @JsonProperty("pksi_list")
    private List<PksiCardItem> pksiList;

    @JsonProperty("fs2_list")
    private List<Fs2CardItem> fs2List;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamSummary {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PksiCardItem {
        private String id;

        @JsonProperty("nama_pksi")
        private String namaPksi;

        @JsonProperty("nama_aplikasi")
        private String namaAplikasi;

        private String status;
        private String progress;

        @JsonProperty("target_go_live")
        private LocalDate targetGoLive;

        @JsonProperty("team_name")
        private String teamName;

        @JsonProperty("jenis_pksi")
        private String jenisPksi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fs2CardItem {
        private String id;

        @JsonProperty("nama_fs2")
        private String namaFs2;

        @JsonProperty("nama_aplikasi")
        private String namaAplikasi;

        private String status;
        private String progres;

        @JsonProperty("progres_status")
        private String progresStatus;

        @JsonProperty("target_go_live")
        private LocalDate targetGoLive;

        @JsonProperty("team_name")
        private String teamName;

        @JsonProperty("fase_pengajuan")
        private String fasePengajuan;
    }
}
