package com.pcs8.orientasi.domain.dto.response;

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
public class InitiativeResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("program_id")
    private UUID programId;

    @JsonProperty("initiative_number")
    private String initiativeNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    @JsonProperty("status")
    private String status;

    @JsonProperty("link_dokumen")
    private String linkDokumen;

    @JsonProperty("tanggal_submit")
    private LocalDateTime tanggalSubmit;

    @JsonProperty("pksi_relation_id")
    private UUID pksiRelationId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
