package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

import static com.pcs8.orientasi.domain.constants.AplikasiFieldNames.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSnapshotRequest {

    @JsonProperty(KODE_APLIKASI)
    @Size(max = 50, message = "Kode aplikasi max 50 characters")
    private String kodeAplikasi;

    @JsonProperty(NAMA_APLIKASI)
    @Size(max = 255, message = "Nama aplikasi max 255 characters")
    private String namaAplikasi;

    @JsonProperty(DESKRIPSI)
    private String deskripsi;

    @JsonProperty(STATUS_APLIKASI)
    private String statusAplikasi; // AKTIF, IDLE, DIAKHIRI, DALAM_PENGEMBANGAN, BELUM_DIKEMBANGKAN

    @JsonProperty(BIDANG_ID)
    private UUID bidangId;

    @JsonProperty(SKPA_ID)
    private UUID skpaId;

    @JsonProperty(TANGGAL_IMPLEMENTASI)
    private LocalDate tanggalImplementasi;

    @JsonProperty(AKSES)
    private String akses; // Comma-separated: INTERNET, INTRANET, EXTRANET, DESKTOP_APP, MOBILE_APP, or custom text

    @JsonProperty(PROSES_DATA_PRIBADI)
    private Boolean prosesDataPribadi;

    @JsonProperty(KETERANGAN_HISTORIS)
    private String keteranganHistoris;

    // Optional changelog fields - if provided, creates a changelog entry
    @JsonProperty("changelog_tanggal")
    private LocalDate changelogTanggal;

    @JsonProperty("changelog_keterangan")
    private String changelogKeterangan;
}
