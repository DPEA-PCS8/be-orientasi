package com.pcs8.orientasi.client.catalog.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogSnapshotStatistikResponse {

    private Integer tahun;

    @JsonProperty("total_aplikasi")
    private Long totalAplikasi;

    @JsonProperty("by_status")
    private Map<String, Long> byStatus;
}
