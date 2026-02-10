package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiRequest {

    @NotBlank(message = "Periode is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Periode must be in format YYYY-YYYY (e.g., 2025-2027)")
    @JsonProperty("periode")
    private String periode;
}
