package com.pcs8.orientasi.domain.dto.request;

import com.pcs8.orientasi.domain.dto.ApprovalFields;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Request untuk update approval fields pada PKSI yang sudah disetujui.
 * Extends ApprovalFields untuk menggunakan field: iku, inhouse_outsource, 
 * pic_approval, pic_approval_name, anggota_tim, anggota_tim_names, progress.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UpdateApprovalRequest extends ApprovalFields {
    // All fields are inherited from ApprovalFields
    // No additional fields needed
}
