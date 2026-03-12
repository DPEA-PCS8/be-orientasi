package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.domain.dto.ApprovalFields;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request untuk update status dokumen PKSI.
 * Extends ApprovalFields untuk field approval yang diisi saat status = DISETUJUI.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateStatusRequest extends ApprovalFields {

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
