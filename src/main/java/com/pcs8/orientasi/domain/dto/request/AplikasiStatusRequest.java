package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AplikasiStatusRequest {

    @NotBlank(message = "Status is required")
    @JsonProperty("status")
    private String status;

    @JsonProperty("tanggal_status")
    private LocalDate tanggalStatus;

    @JsonProperty("idle_info")
    @Valid
    private IdleRequest idleInfo;
}
