package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiMonitoringResponse {

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("periode")
    private String periode;

    @JsonProperty("kep_list")
    private List<KepInfo> kepList;

    @JsonProperty("programs")
    private List<ProgramMonitoring> programs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KepInfo {
        @JsonProperty("id")
        private UUID id;

        @JsonProperty("nomor_kep")
        private String nomorKep;

        @JsonProperty("tahun_pelaporan")
        private Integer tahunPelaporan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramMonitoring {
        @JsonProperty("nomor_program")
        private String nomorProgram;

        @JsonProperty("versions_by_year")
        private Map<Integer, ProgramVersion> versionsByYear;

        @JsonProperty("initiatives")
        private List<InitiativeMonitoring> initiatives;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramVersion {
        @JsonProperty("id")
        private UUID id;

        @JsonProperty("nama_program")
        private String namaProgram;

        @JsonProperty("tahun")
        private Integer tahun;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiativeMonitoring {
        @JsonProperty("group_id")
        private UUID groupId;

        @JsonProperty("versions_by_year")
        private Map<Integer, InitiativeVersion> versionsByYear;

        @JsonProperty("progress_by_kep")
        private Map<UUID, KepProgress> progressByKep;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiativeVersion {
        @JsonProperty("id")
        private UUID id;

        @JsonProperty("nomor_inisiatif")
        private String nomorInisiatif;

        @JsonProperty("nama_inisiatif")
        private String namaInisiatif;

        @JsonProperty("tahun")
        private Integer tahun;

        @JsonProperty("status_badge")
        private String statusBadge; // "new" | "modified" | null
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KepProgress {
        @JsonProperty("yearly_progress")
        private List<YearlyProgress> yearlyProgress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyProgress {
        @JsonProperty("tahun")
        private Integer tahun;

        @JsonProperty("status")
        private String status;
    }
}
