package com.pcs8.orientasi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class containing shared approval fields.
 * Used by UpdateStatusRequest and PksiDocumentResponse to reduce code duplication.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class ApprovalFields {

    @JsonProperty("iku")
    protected String iku;

    @JsonProperty("inhouse_outsource")
    protected String inhouseOutsource;

    @JsonProperty("pic_approval")
    protected String picApproval;

    @JsonProperty("pic_approval_name")
    protected String picApprovalName;

    @JsonProperty("anggota_tim")
    protected String anggotaTim;

    @JsonProperty("anggota_tim_names")
    protected String anggotaTimNames;

    @JsonProperty("progress")
    protected String progress;
}
