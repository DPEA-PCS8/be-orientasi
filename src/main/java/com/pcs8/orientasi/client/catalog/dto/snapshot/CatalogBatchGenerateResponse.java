package com.pcs8.orientasi.client.catalog.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogBatchGenerateResponse {

    private Integer tahun;

    @JsonProperty("total_generated")
    private Integer totalGenerated;

    @JsonProperty("total_updated")
    private Integer totalUpdated;
}
