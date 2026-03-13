package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchKepProgressResponse {

    @JsonProperty("total_updated")
    private Integer totalUpdated;

    @JsonProperty("updated_progress")
    private List<KepProgressResponse> updatedProgress;

    @JsonProperty("message")
    private String message;
}
