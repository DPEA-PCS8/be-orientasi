package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for RBSI Dashboard showing initiative-PKSI relationship insights
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiDashboardResponse {

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("periode")
    private String periode;

    @JsonProperty("selected_tahun")
    private Integer selectedTahun;

    @JsonProperty("comparison_tahun")
    private Integer comparisonTahun;

    @JsonProperty("summary")
    private DashboardSummary summary;

    @JsonProperty("initiatives")
    private List<InisiatifPksiDetail> initiatives;

    @JsonProperty("available_years")
    private List<Integer> availableYears;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        @JsonProperty("total_inisiatif")
        private Integer totalInisiatif;

        @JsonProperty("inisiatif_with_pksi")
        private Integer inisiatifWithPksi;

        @JsonProperty("inisiatif_without_pksi")
        private Integer inisiatifWithoutPksi;

        @JsonProperty("percentage_with_pksi")
        private Double percentageWithPksi;

        // KEP Progress Expected vs Actual
        @JsonProperty("kep_expected_with_pksi")
        private Integer kepExpectedWithPksi;

        @JsonProperty("kep_realized_with_pksi")
        private Integer kepRealizedWithPksi;

        @JsonProperty("kep_missing_pksi")
        private Integer kepMissingPksi;

        @JsonProperty("kep_unexpected_pksi")
        private Integer kepUnexpectedPksi;

        @JsonProperty("kep_compliance_percentage")
        private Double kepCompliancePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InisiatifPksiDetail {
        @JsonProperty("group_id")
        private UUID groupId;

        @JsonProperty("nama_inisiatif")
        private String namaInisiatif;

        @JsonProperty("nomor_inisiatif")
        private String nomorInisiatif;

        @JsonProperty("program_nama")
        private String programNama;

        @JsonProperty("has_pksi")
        private Boolean hasPksi;

        @JsonProperty("pksi_list")
        private List<PksiInfo> pksiList;

        @JsonProperty("kep_progress_comparison")
        private KepProgressComparison kepProgressComparison;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PksiInfo {
        @JsonProperty("id")
        private UUID id;

        @JsonProperty("nama_pksi")
        private String namaPksi;

        @JsonProperty("status")
        private String status;

        @JsonProperty("tahun_pelaksanaan_awal")
        private Integer tahunPelaksanaanAwal;

        @JsonProperty("tahun_pelaksanaan_akhir")
        private Integer tahunPelaksanaanAkhir;

        @JsonProperty("is_multiyear")
        private Boolean isMultiyear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KepProgressComparison {
        @JsonProperty("yearly_status")
        private Map<Integer, YearlyKepStatus> yearlyStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyKepStatus {
        @JsonProperty("tahun")
        private Integer tahun;

        /**
         * Status from KEP progress (planned/realized/none)
         */
        @JsonProperty("kep_status")
        private String kepStatus;

        /**
         * Whether the initiative has PKSI in this year
         */
        @JsonProperty("has_pksi_in_year")
        private Boolean hasPksiInYear;

        /**
         * Discrepancy type:
         * - null: no discrepancy
         * - "SHOULD_HAVE_PKSI": KEP shows planned/realized but no PKSI exists
         * - "UNEXPECTED_PKSI": KEP shows none but PKSI exists
         */
        @JsonProperty("discrepancy_type")
        private String discrepancyType;

        /**
         * Message explaining the discrepancy
         */
        @JsonProperty("discrepancy_message")
        private String discrepancyMessage;

        /**
         * Whether this year should be highlighted
         */
        @JsonProperty("is_highlighted")
        private Boolean isHighlighted;
    }
}
