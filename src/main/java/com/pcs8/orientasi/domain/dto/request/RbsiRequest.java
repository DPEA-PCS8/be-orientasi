package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RbsiRequest {

    @JsonProperty("periode")
    @NotBlank(message = "Periode is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Periode must use YYYY-YYYY format")
    private String periode;
}
