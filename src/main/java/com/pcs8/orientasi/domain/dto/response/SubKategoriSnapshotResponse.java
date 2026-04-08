package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubKategoriSnapshotResponse {

    private UUID id;

    @JsonProperty("snapshot_year")
    private Integer snapshotYear;

    @JsonProperty("sub_kategori_id")
    private UUID subKategoriId;

    @JsonProperty("kode")
    private String kode;

    @JsonProperty("nama")
    private String nama;

    @JsonProperty("category_code")
    private String categoryCode;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("snapshot_date")
    private LocalDateTime snapshotDate;

    @JsonProperty("change_type")
    private String changeType;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("created_by")
    private String createdBy;
}
