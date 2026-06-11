package com.pcs8.orientasi.client.catalog.dto.aplikasi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private IdleInfoRequest idleInfo;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IdleInfoRequest {
        @JsonProperty("kategori_idle")
        private String kategoriIdle;

        @JsonProperty("alasan_idle")
        private String alasanIdle;

        @JsonProperty("rencana_pengakhiran")
        private String rencanaPengakhiran;

        @JsonProperty("alasan_belum_diakhiri")
        private String alasanBelumDiakhiri;
    }
}
