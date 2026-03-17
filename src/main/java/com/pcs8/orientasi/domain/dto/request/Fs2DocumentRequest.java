package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcs8.orientasi.domain.dto.base.Fs2FormFieldsBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Fs2DocumentRequest extends Fs2FormFieldsBase {

    @JsonProperty("aplikasi_id")
    private UUID aplikasiId;

    @JsonProperty("tanggal_pengajuan")
    private LocalDate tanggalPengajuan;

    @JsonProperty("bidang_id")
    private UUID bidangId;

    @JsonProperty("skpa_id")
    private UUID skpaId;

    // Status: PENDING, DISETUJUI, TIDAK_DISETUJUI
    private String status;

    // All form fields are inherited from Fs2FormFieldsBase
}
