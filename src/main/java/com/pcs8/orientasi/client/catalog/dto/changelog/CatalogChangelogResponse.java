package com.pcs8.orientasi.client.catalog.dto.changelog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogChangelogResponse {

    private UUID id;

    @JsonProperty("tanggal_perubahan")
    private LocalDate tanggalPerubahan;

    private String keterangan;

    @JsonProperty("perubahan_detail")
    private String perubahanDetail;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
