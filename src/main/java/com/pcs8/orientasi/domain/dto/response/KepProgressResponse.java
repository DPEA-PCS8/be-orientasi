package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KepProgressResponse {

    private UUID id;

    @JsonProperty("kep_id")
    private UUID kepId;

    @JsonProperty("nomor_kep")
    private String nomorKep;

    @JsonProperty("group_id")
    private UUID groupId;

    @JsonProperty("yearly_progress")
    private List<YearlyProgressResponse> yearlyProgress;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyProgressResponse {

        @JsonProperty("tahun")
        private Integer tahun;

        @JsonProperty("status")
        private String status;
    }
}
