package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiAnalyticsRequest {

    @NotNull(message = "Tahun pertama harus diisi")
    @JsonProperty("tahun_1")
    private Integer tahun1;

    @NotNull(message = "Tahun kedua harus diisi")
    @JsonProperty("tahun_2")
    private Integer tahun2;
}
