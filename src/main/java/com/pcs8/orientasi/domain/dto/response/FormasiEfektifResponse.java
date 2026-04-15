package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Formasi Efektif Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormasiEfektifResponse {

    @JsonProperty("selected_tahun")
    private Integer selectedTahun;

    @JsonProperty("available_years")
    private List<Integer> availableYears;

    @JsonProperty("summary")
    private FormasiSummary summary;

    @JsonProperty("developer_list")
    private List<DeveloperItem> developerList;

    @JsonProperty("parameters")
    private List<ParameterItem> parameters;

    /**
     * Summary of Formasi Efektif calculations
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormasiSummary {

        @JsonProperty("formasi_efektif")
        private FormasiByLevel formasiEfektif;

        @JsonProperty("formasi_saat_ini")
        private FormasiByLevel formasiSaatIni;

        @JsonProperty("kebutuhan")
        private FormasiByLevel kebutuhan;
    }

    /**
     * Formasi breakdown by level (Manajer & Asisten Manajer)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormasiByLevel {

        @JsonProperty("manajer")
        private Double manajer;

        @JsonProperty("asisten_manajer")
        private Double asistenManajer;

        @JsonProperty("total")
        private Double total;
    }

    /**
     * Developer item in the list
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeveloperItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("username")
        private String username;

        @JsonProperty("title")
        private String title;

        @JsonProperty("level")
        private String level; // MANAJER or ASISTEN_MANAJER

        @JsonProperty("pksi_count")
        private Integer pksiCount;

        @JsonProperty("fs2_count")
        private Integer fs2Count;

        @JsonProperty("pksi_list")
        private List<WorkItem> pksiList;

        @JsonProperty("fs2_list")
        private List<WorkItem> fs2List;
    }

    /**
     * Work item (PKSI or FS2) for developer
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("team_name")
        private String teamName;
    }

    /**
     * Configuration parameter item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("kode")
        private String kode;

        @JsonProperty("nama")
        private String nama;

        @JsonProperty("deskripsi")
        private String deskripsi;

        @JsonProperty("nilai")
        private String nilai;

        @JsonProperty("urutan")
        private Integer urutan;
    }
}
