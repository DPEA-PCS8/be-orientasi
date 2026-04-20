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
 * Supports new status DIKERJAKAN_DENGAN_CARA_LAIN with parent_pksi_id.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateStatusRequest extends ApprovalFields {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|DISETUJUI|DITOLAK|DIKERJAKAN_DENGAN_CARA_LAIN", message = "Invalid status value")
    @JsonProperty("status")
    private String status;

    /**
     * Required when status = DIKERJAKAN_DENGAN_CARA_LAIN.
     * The UUID of the parent PKSI that this PKSI will follow.
     */
    @JsonProperty("parent_pksi_id")
    private String parentPksiId;
}
