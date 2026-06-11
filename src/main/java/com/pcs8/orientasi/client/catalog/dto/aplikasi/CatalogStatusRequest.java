package com.pcs8.orientasi.client.catalog.dto.aplikasi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogIdleInfoRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogStatusRequest {

    private String status;

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

    @JsonProperty("idle_info")
    private CatalogIdleInfoRequest idleInfo;
}
