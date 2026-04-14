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
 * Response DTO for PKSI Dashboard showing insights and analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PksiDashboardResponse {

    @JsonProperty("selected_tahun")
    private Integer selectedTahun;

    @JsonProperty("selected_bulan")
    private Integer selectedBulan;

    @JsonProperty("available_years")
    private List<Integer> availableYears;

    @JsonProperty("available_months")
    private List<MonthOption> availableMonths;

    @JsonProperty("snapshot_date")
    private String snapshotDate;

    @JsonProperty("summary")
    private DashboardSummary summary;

    @JsonProperty("approval_breakdown")
    private ApprovalBreakdown approvalBreakdown;

    @JsonProperty("progress_by_bidang")
    private List<ProgressByBidangRow> progressByBidang;

    @JsonProperty("bidang_list")
    private List<BidangItem> bidangList;

    @JsonProperty("progress_insights")
    private ProgressInsights progressInsights;

    @JsonProperty("jenis_pksi_stats")
    private JenisPksiStats jenisPksiStats;

    @JsonProperty("pelaksana_stats")
    private PelaksanaStats pelaksanaStats;

    @JsonProperty("bidang_stats")
    private List<BidangStat> bidangStats;

    @JsonProperty("pksi_list")
    private List<PksiListItem> pksiList;

    @JsonProperty("monthly_progress_trend")
    private List<MonthlyProgressTrend> monthlyProgressTrend;

    // ==================== NESTED CLASSES ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthOption {
        @JsonProperty("value")
        private Integer value;
        
        @JsonProperty("label")
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        @JsonProperty("total_pksi")
        private Integer totalPksi;

        @JsonProperty("total_disetujui")
        private Integer totalDisetujui;

        @JsonProperty("total_pending")
        private Integer totalPending;

        @JsonProperty("total_ditolak")
        private Integer totalDitolak;

        @JsonProperty("percentage_disetujui")
        private Double percentageDisetujui;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalBreakdown {
        @JsonProperty("disetujui_tahun_ini")
        private Integer disetujuiTahunIni;

        @JsonProperty("disetujui_multiyears_sebelumnya")
        private Integer disetujuiMultiyearsSebelumnya;

        @JsonProperty("disetujui_mendesak")
        private Integer disetujuiMendesak;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressByBidangRow {
        @JsonProperty("progress")
        private String progress;

        @JsonProperty("progress_label")
        private String progressLabel;

        @JsonProperty("counts_by_bidang")
        private Map<String, Integer> countsByBidang;

        @JsonProperty("total")
        private Integer total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidangItem {
        @JsonProperty("id")
        private UUID id;

        @JsonProperty("kode_bidang")
        private String kodeBidang;

        @JsonProperty("nama_bidang")
        private String namaBidang;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInsights {
        /**
         * Progress stages grouped by phase
         */
        @JsonProperty("early_stage")
        private PhaseDetail earlyStage;  // Usreq, Pengadaan
        
        @JsonProperty("development_stage")
        private PhaseDetail developmentStage;  // Desain, Coding, Unit Test
        
        @JsonProperty("testing_stage")
        private PhaseDetail testingStage;  // SIT, UAT
        
        @JsonProperty("deployment_stage")
        private PhaseDetail deploymentStage;  // Deployment, Selesai

        @JsonProperty("deadline_current_year")
        private DeadlineInsight deadlineCurrentYear;

        @JsonProperty("deadline_next_year")
        private DeadlineInsight deadlineNextYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhaseDetail {
        @JsonProperty("label")
        private String label;
        
        @JsonProperty("total")
        private Integer total;
        
        @JsonProperty("percentage")
        private Double percentage;
        
        @JsonProperty("progress_breakdown")
        private List<ProgressCount> progressBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressCount {
        @JsonProperty("progress")
        private String progress;
        
        @JsonProperty("count")
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeadlineInsight {
        @JsonProperty("year")
        private Integer year;
        
        @JsonProperty("total")
        private Integer total;
        
        @JsonProperty("label")
        private String label;
        
        @JsonProperty("progress_breakdown")
        private List<ProgressCount> progressBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JenisPksiStats {
        @JsonProperty("single_year")
        private Integer singleYear;

        @JsonProperty("multiyears_y_minus1")
        private Integer multiyearsYMinus1;

        @JsonProperty("multiyears_y_plus1")
        private Integer multiyearsYPlus1;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PelaksanaStats {
        @JsonProperty("inhouse")
        private Integer inhouse;

        @JsonProperty("outsource")
        private Integer outsource;

        @JsonProperty("unknown")
        private Integer unknown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidangStat {
        @JsonProperty("bidang_kode")
        private String bidangKode;

        @JsonProperty("bidang_nama")
        private String bidangNama;

        @JsonProperty("count")
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PksiListItem {
        @JsonProperty("id")
        private UUID id;

        @JsonProperty("nama_pksi")
        private String namaPksi;

        @JsonProperty("inisiatif_nomor")
        private String inisiatifNomor;

        @JsonProperty("inisiatif_nama")
        private String inisiatifNama;

        @JsonProperty("status")
        private String status;

        @JsonProperty("progress")
        private String progress;

        @JsonProperty("bidang_nama")
        private String bidangNama;

        @JsonProperty("tahap7_awal")
        private String tahap7Awal;

        @JsonProperty("tahap7_akhir")
        private String tahap7Akhir;

        @JsonProperty("is_multiyear")
        private Boolean isMultiyear;

        @JsonProperty("inhouse_outsource")
        private String inhouseOutsource;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyProgressTrend {
        @JsonProperty("month")
        private Integer month;

        @JsonProperty("month_label")
        private String monthLabel;

        @JsonProperty("early_stage")
        private Integer earlyStage;

        @JsonProperty("development_stage")
        private Integer developmentStage;

        @JsonProperty("testing_stage")
        private Integer testingStage;

        @JsonProperty("completed")
        private Integer completed;
    }
}
