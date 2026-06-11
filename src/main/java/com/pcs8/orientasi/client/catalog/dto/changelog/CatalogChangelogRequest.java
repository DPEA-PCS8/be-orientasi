package com.pcs8.orientasi.client.catalog.dto.changelog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogChangelogRequest {

    @JsonProperty("tanggal_perubahan")
    private LocalDate tanggalPerubahan;

    private String keterangan;

    @JsonProperty("perubahan_detail")
    private String perubahanDetail;
}
