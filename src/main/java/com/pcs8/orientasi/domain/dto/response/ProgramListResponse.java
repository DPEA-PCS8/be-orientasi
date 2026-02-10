package com.pcs8.orientasi.domain.dto.response;

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
public class ProgramListResponse {

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("periode")
    private String periode;

    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("total_programs")
    private Integer totalPrograms;

    @JsonProperty("programs")
    private List<ProgramResponse> programs;
}
