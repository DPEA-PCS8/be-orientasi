package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KepProgressFullResponse {

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("periode")
    private String periode;

    @JsonProperty("kep_list")
    private List<RbsiKepResponse> kepList;

    @JsonProperty("progress")
    private List<InisiatifKepProgress> progress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InisiatifKepProgress {

        @JsonProperty("inisiatif_id")
        private UUID inisiatifId;

        @JsonProperty("kep_progress")
        private List<KepProgressItem> kepProgress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KepProgressItem {

        @JsonProperty("kep_id")
        private UUID kepId;

        @JsonProperty("nomor_kep")
        private String nomorKep;

        @JsonProperty("tahun_pelaporan")
        private Integer tahunPelaporan;

        @JsonProperty("yearly_progress")
        private List<KepProgressResponse.YearlyProgressResponse> yearlyProgress;
    }
}
