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

    @JsonProperty("inisiatif_group_id")
    private String inisiatifGroupId;

    @JsonProperty("inisiatif_nomor")
    private String inisiatifNomor;

    @JsonProperty("inisiatif_nama")
    private String inisiatifNama;

    @JsonProperty("tujuan_pengajuan")
    private String tujuanPengajuan;

    @JsonProperty("kapan_diselesaikan")
    private String kapanDiselesaikan;

    @JsonProperty("pic_satker")
    private String picSatker;

    @JsonProperty("pic_satker_names")
    private String picSatkerNames;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
