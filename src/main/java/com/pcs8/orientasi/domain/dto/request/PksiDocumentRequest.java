package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.domain.dto.PksiDocumentFields;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request untuk dokumen T.01 (PKSI) - extends shared fields
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PksiDocumentRequest extends PksiDocumentFields {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("tujuan_pengajuan")
    private String tujuanPengajuan;

    @JsonProperty("kapan_diselesaikan")
    private String kapanDiselesaikan;

    @JsonProperty("pic_satker")
    private String picSatker;
}
