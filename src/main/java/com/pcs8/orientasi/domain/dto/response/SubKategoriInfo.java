package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubKategoriInfo {

    private UUID id;

    @JsonProperty("kode")
    private String kode;

    @JsonProperty("nama")
    private String nama;

    @JsonProperty("category_code")
    private String categoryCode;

    @JsonProperty("category_name")
    private String categoryName;
}
