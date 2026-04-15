package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Formasi Efektif Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormasiEfektifRequest {

    @JsonProperty("tahun")
    private Integer tahun;
}
