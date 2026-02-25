package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KepProgressRequest {

    @JsonProperty("inisiatif_id")
    @NotNull(message = "Inisiatif ID is required")
    private UUID inisiatifId;

    @JsonProperty("yearly_progress")
    @Valid
    @NotNull(message = "Yearly progress is required")
    private List<YearlyProgressItem> yearlyProgress;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YearlyProgressItem {

        @JsonProperty("tahun")
        @NotNull(message = "Tahun is required")
        private Integer tahun;

        @JsonProperty("status")
        @NotNull(message = "Status is required")
        private String status;
    }
}
