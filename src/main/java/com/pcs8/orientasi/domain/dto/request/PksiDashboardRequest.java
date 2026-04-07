package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for PKSI Dashboard filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PksiDashboardRequest {

    /**
     * Filter by year (based on tahap timeline dates)
     */
    @JsonProperty("tahun")
    private Integer tahun;

    /**
     * Filter by month (1-12) for historical progress view
     */
    @JsonProperty("bulan")
    private Integer bulan;

    /**
     * Filter PKSI by status. Options:
     * - null or empty: all PKSI
     * - "DISETUJUI": only approved PKSI
     * - "PENDING": only pending PKSI
     */
    @JsonProperty("status")
    private String status;
}
