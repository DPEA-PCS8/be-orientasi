package com.pcs8.orientasi.domain.dto.rbsi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RbsiCreateRequest {

    @NotBlank(message = "Periode tidak boleh kosong")
    @Size(max = 50, message = "Periode maksimal 50 karakter")
    @JsonProperty("periode")
    private String periode;
}
