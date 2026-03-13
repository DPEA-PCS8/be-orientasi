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
public class RbsiAnalyticsResponse {

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("periode")
    private String periode;

    @JsonProperty("tahun_list")
    private List<Integer> tahunList; // All years from RBSI period (e.g., 2023-2027)

    @JsonProperty("evaluations")
    private List<KepEvaluation> evaluations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KepEvaluation {
        @JsonProperty("kep_id")
        private UUID kepId;

        @JsonProperty("nomor_kep")
        private String nomorKep;

        @JsonProperty("tahun_pelaporan")
        private Integer tahunPelaporan;

        @JsonProperty("total")
        private Integer total; // Total realized count across all years

        @JsonProperty("count_by_year")
        private Map<Integer, Integer> countByYear; // e.g., {2023: 40, 2024: 51, 2025: 55, ...}

        @JsonProperty("changes")
        private ProgressChanges changes; // Comparison with previous KEP (if any)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressChanges {
        @JsonProperty("has_changes")
        private Boolean hasChanges;

        @JsonProperty("changes_by_year")
        private Map<Integer, YearChange> changesByYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearChange {
        @JsonProperty("added")
        private Integer added; // Number of progress added (none → realized)

        @JsonProperty("removed")
        private Integer removed; // Number of progress removed (realized → none)

        @JsonProperty("summary")
        private String summary; // e.g., "+6 inisiatif, -13 inisiatif"
    }
}
