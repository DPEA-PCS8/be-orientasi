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
public class RbsiHistoryResponse {

    @JsonProperty("tahun")
    private Integer tahun;

    @JsonProperty("programs")
    private List<RbsiProgramResponse> programs;
}
