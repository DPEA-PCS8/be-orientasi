package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.domain.dto.base.Fs2FormFieldsBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Fs2DocumentResponse extends Fs2FormFieldsBase {

    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    @JsonProperty("nama_aplikasi")
    private String namaAplikasi;

    @JsonProperty("kode_aplikasi")
    private String kodeAplikasi;

    @JsonProperty("tanggal_pengajuan")
    private LocalDate tanggalPengajuan;

    @JsonProperty("bidang_id")
    private UUID bidangId;

    @JsonProperty("nama_bidang")
    private String namaBidang;

    @JsonProperty("skpa_id")
    private UUID skpaId;

    @JsonProperty("nama_skpa")
    private String namaSkpa;

    @JsonProperty("kode_skpa")
    private String kodeSkpa;

    private String status;

    // Response-specific fields only (form fields inherited from Fs2FormFieldsBase)

    @JsonProperty("pic_name")
    private String picName;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
