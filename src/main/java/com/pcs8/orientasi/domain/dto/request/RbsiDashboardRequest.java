package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for RBSI Dashboard filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiDashboardRequest {

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("tahun")
    private Integer tahun;

    /**
     * Filter PKSI by status. Options:
     * - null or empty: all PKSI
     * - "DISETUJUI": only approved PKSI
     * - "PENDING": only pending PKSI
     */
    @JsonProperty("pksi_status")
    private String pksiStatus;

    /**
     * Comparison year for KEP progress
     * Default is previous year if not specified
     */
    @JsonProperty("comparison_year")
    private Integer comparisonYear;

    /**
     * Filter by specific KEP Progress ID
     * If null, use all KEP for this RBSI
     */
    @JsonProperty("kep_id")
    private UUID kepId;
}
