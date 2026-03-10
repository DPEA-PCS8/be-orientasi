package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AplikasiStatistikResponse {

    @JsonProperty("tahun")
    private Integer tahun;

    @JsonProperty("total_aplikasi")
    private Long totalAplikasi;

    @JsonProperty("by_status")
    private Map<String, Long> byStatus;
}
