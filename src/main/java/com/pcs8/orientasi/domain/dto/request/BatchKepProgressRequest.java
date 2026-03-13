package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchKepProgressRequest {

    @JsonProperty("updates")
    @Valid
    @NotEmpty(message = "Updates list cannot be empty")
    private List<KepProgressUpdate> updates;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KepProgressUpdate {

        @JsonProperty("kep_id")
        private UUID kepId;

        @JsonProperty("group_id")
        private UUID groupId;

        @JsonProperty("yearly_progress")
        @Valid
        private List<YearlyProgressItem> yearlyProgress;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class YearlyProgressItem {

            @JsonProperty("tahun")
            private Integer tahun;

            @JsonProperty("status")
            private String status;
        }
    }
}
