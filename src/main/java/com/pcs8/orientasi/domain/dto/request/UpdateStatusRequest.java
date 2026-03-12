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
public class UpdateStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|DISETUJUI|DITOLAK", message = "Invalid status value")
    @JsonProperty("status")
    private String status;

    // Approval fields (required when status = DISETUJUI)
    @JsonProperty("iku")
    private String iku;

    @JsonProperty("inhouse_outsource")
    private String inhouseOutsource;

    @JsonProperty("pic_approval")
    private String picApproval;

    @JsonProperty("anggota_tim")
    private String anggotaTim;
}
