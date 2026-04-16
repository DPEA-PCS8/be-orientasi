package com.pcs8.orientasi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PksiBulkInsertResponse {

    @JsonProperty("total_rows")
    private int totalRows;

    @JsonProperty("success_count")
    private int successCount;

    @JsonProperty("failed_count")
    private int failedCount;

    @JsonProperty("errors")
    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        @JsonProperty("row_number")
        private int rowNumber;

        @JsonProperty("error_message")
        private String errorMessage;
    }
}
