package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Formasi Efektif Detail Page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormasiEfektifDetailResponse {

    @JsonProperty("selected_tahun")
    private Integer selectedTahun;

    @JsonProperty("available_years")
    private List<Integer> availableYears;

    @JsonProperty("summary")
    private CalculationSummary summary;

    @JsonProperty("pksi_details")
    private List<PksiDetailItem> pksiDetails;

    @JsonProperty("fs2_details")
    private List<Fs2DetailItem> fs2Details;

    @JsonProperty("parameters")
    private List<FormasiEfektifResponse.ParameterItem> parameters;

    /**
     * Calculation summary with breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculationSummary {

        @JsonProperty("pksi_man_hour")
        private ManHourByLevel pksiManHour;

        @JsonProperty("fs2_man_hour")
        private ManHourByLevel fs2ManHour;

        @JsonProperty("maintenance_man_hour")
        private ManHourByLevel maintenanceManHour;

        @JsonProperty("maintenance_base_count")
        private Integer maintenanceBaseCount;

        @JsonProperty("total_man_hour")
        private ManHourByLevel totalManHour;

        @JsonProperty("formasi_efektif")
        private FormasiEfektifResponse.FormasiByLevel formasiEfektif;

        @JsonProperty("formasi_saat_ini")
        private FormasiEfektifResponse.FormasiByLevel formasiSaatIni;

        @JsonProperty("kebutuhan")
        private FormasiEfektifResponse.FormasiByLevel kebutuhan;
    }

    /**
     * Man hour breakdown by level
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManHourByLevel {

        @JsonProperty("manajer")
        private Double manajer;

        @JsonProperty("asisten_manajer")
        private Double asistenManajer;

        @JsonProperty("total")
        private Double total;
    }

    /**
     * PKSI detail item with calculation breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PksiDetailItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("nama_pksi")
        private String namaPksi;

        @JsonProperty("nama_aplikasi")
        private String namaAplikasi;

        @JsonProperty("jenis_pksi")
        private String jenisPksi;

        @JsonProperty("inhouse_outsource")
        private String inhouseOutsource;

        @JsonProperty("workload_pct")
        private Double workloadPct;

        @JsonProperty("usreq_date")
        private String usreqDate;

        @JsonProperty("uat_date")
        private String uatDate;

        @JsonProperty("duration_months")
        private Integer durationMonths;

        @JsonProperty("man_hour")
        private Double manHour;

        @JsonProperty("man_hour_manajer")
        private Double manHourManajer;

        @JsonProperty("man_hour_asman")
        private Double manHourAsman;
    }

    /**
     * FS2 detail item with calculation breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fs2DetailItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("nama_aplikasi")
        private String namaAplikasi;

        @JsonProperty("deskripsi_pengubahan")
        private String deskripsiPengubahan;

        @JsonProperty("mekanisme")
        private String mekanisme;

        @JsonProperty("workload_pct")
        private Double workloadPct;

        @JsonProperty("tanggal_pengajuan")
        private String tanggalPengajuan;

        @JsonProperty("target_go_live")
        private String targetGoLive;

        @JsonProperty("duration_months")
        private Integer durationMonths;

        @JsonProperty("man_hour")
        private Double manHour;

        @JsonProperty("man_hour_manajer")
        private Double manHourManajer;

        @JsonProperty("man_hour_asman")
        private Double manHourAsman;
    }
}
