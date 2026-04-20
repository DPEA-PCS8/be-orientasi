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
 * Includes nested PKSI (parent-child relationship) fields
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

    @JsonProperty("inisiatif_id")
    private String inisiatifId;

    @JsonProperty("inisiatif_nomor")
    private String inisiatifNomor;

    @JsonProperty("inisiatif_nama")
    private String inisiatifNama;

    @JsonProperty("inisiatif_tahun")
    private Integer inisiatifTahun;

    @JsonProperty("team_name")
    private String teamName;

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

    // ==================== NESTED PKSI FIELDS ====================

    /**
     * Flag indicating this PKSI is a nested/child PKSI.
     * True when status = DIKERJAKAN_DENGAN_CARA_LAIN and has a parent.
     */
    @JsonProperty("is_nested_pksi")
    private Boolean isNestedPksi;

    /**
     * Parent PKSI ID (UUID string).
     * Set when this PKSI follows another PKSI.
     */
    @JsonProperty("parent_pksi_id")
    private String parentPksiId;

    /**
     * Parent PKSI name for display purposes.
     */
    @JsonProperty("parent_pksi_nama")
    private String parentPksiNama;

    /**
     * Count of child PKSI documents that follow this PKSI.
     */
    @JsonProperty("child_count")
    private Integer childCount;

    /**
     * List of child PKSI documents that follow this PKSI.
     */
    @JsonProperty("child_pksi_list")
    private java.util.List<ChildPksiSummary> childPksiList;

    /**
     * Summary information for child PKSI
     */
    @Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildPksiSummary {
        @JsonProperty("id")
        private String id;

        @JsonProperty("nama_pksi")
        private String namaPksi;

        @JsonProperty("status")
        private String status;

        @JsonProperty("nama_aplikasi")
        private String namaAplikasi;

        @JsonProperty("tanggal_pengajuan")
        private String tanggalPengajuan;
    }
}
