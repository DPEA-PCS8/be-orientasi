package com.pcs8.orientasi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

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

    @JsonProperty("team_id")
    protected String teamId;

    // Monitoring fields - Anggaran
    @JsonProperty("anggaran_total")
    protected String anggaranTotal;

    @JsonProperty("anggaran_tahun_ini")
    protected String anggaranTahunIni;

    @JsonProperty("anggaran_tahun_depan")
    protected String anggaranTahunDepan;

    // Monitoring fields - Target Timeline (new flexible structure)
    @JsonProperty("timelines")
    protected List<PksiTimelineDto> timelines;

    // Monitoring fields - Target Timeline (DEPRECATED - kept for backward compatibility)
    @JsonProperty("target_usreq")
    protected LocalDate targetUsreq;

    @JsonProperty("target_sit")
    protected LocalDate targetSit;

    @JsonProperty("target_uat")
    protected LocalDate targetUat;

    @JsonProperty("target_go_live")
    protected LocalDate targetGoLive;

    // Monitoring fields - T01/T02 Status
    @JsonProperty("status_t01_t02")
    protected String statusT01T02;

    @JsonProperty("berkas_t01_t02")
    protected String berkasT01T02;

    // Monitoring fields - T11 Status
    @JsonProperty("status_t11")
    protected String statusT11;

    @JsonProperty("berkas_t11")
    protected String berkasT11;

    // Monitoring fields - CD Prinsip
    @JsonProperty("status_cd")
    protected String statusCd;

    @JsonProperty("nomor_cd")
    protected String nomorCd;

    // Monitoring fields - Kontrak
    @JsonProperty("kontrak_tanggal_mulai")
    protected String kontrakTanggalMulai;

    @JsonProperty("kontrak_tanggal_selesai")
    protected String kontrakTanggalSelesai;

    @JsonProperty("kontrak_nilai")
    protected String kontrakNilai;

    @JsonProperty("kontrak_jumlah_termin")
    protected String kontrakJumlahTermin;

    @JsonProperty("kontrak_detail_pembayaran")
    protected String kontrakDetailPembayaran;

    // Monitoring fields - BA Deploy
    @JsonProperty("ba_deploy")
    protected String baDeploy;
}
