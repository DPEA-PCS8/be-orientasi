package com.pcs8.orientasi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for FS2 Timeline item
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Fs2TimelineDto {

    @JsonProperty("phase")
    private Integer phase;

    @JsonProperty("target_date")
    private String targetDate;  // ISO date string (YYYY-MM-DD)

    @JsonProperty("stage")
    private String stage;  // PENGAJUAN, ASESMEN, PEMROGRAMAN, PENGUJIAN, DEPLOYMENT, GO_LIVE
}
