package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.domain.dto.PksiDocumentFields;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Response untuk dokumen T.01 (PKSI) - extends shared fields
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PksiDocumentResponse extends PksiDocumentFields {

    @JsonProperty("id")
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("aplikasi_id")
    private String aplikasiId;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    @JsonProperty("tujuan_pengajuan")
    private String tujuanPengajuan;

    @JsonProperty("kapan_diselesaikan")
    private String kapanDiselesaikan;

    @JsonProperty("pic_satker")
    private String picSatker;

    @JsonProperty("status")
    private String status;

    // Approval fields
    @JsonProperty("iku")
    private String iku;

    @JsonProperty("inhouse_outsource")
    private String inhouseOutsource;

    @JsonProperty("pic_approval")
    private String picApproval;

    @JsonProperty("anggota_tim")
    private String anggotaTim;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
