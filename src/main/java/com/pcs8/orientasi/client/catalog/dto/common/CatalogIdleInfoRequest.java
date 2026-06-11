package com.pcs8.orientasi.client.catalog.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogIdleInfoRequest {

    @JsonProperty("kategori_idle")
    private String kategoriIdle;

    @JsonProperty("alasan_idle")
    private String alasanIdle;

    @JsonProperty("rencana_pengakhiran")
    private String rencanaPengakhiran;

    @JsonProperty("alasan_belum_diakhiri")
    private String alasanBelumDiakhiri;
}
