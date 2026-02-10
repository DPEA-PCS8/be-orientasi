package com.pcs8.orientasi.domain.dto.rbsi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiativeResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("program_id")
    private UUID programId;

    @JsonProperty("initiative_number")
    private String initiativeNumber;

    @JsonProperty("sequence_order")
    private Integer sequenceOrder;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("submit_date")
    private LocalDateTime submitDate;

    @JsonProperty("document_link")
    private String documentLink;

    @JsonProperty("status")
    private String status;

    @JsonProperty("pksi_relation_id")
    private UUID pksiRelationId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
