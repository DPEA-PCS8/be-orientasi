package com.pcs8.orientasi.domain.dto.rbsi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("program_number")
    private String programNumber;

    @JsonProperty("sequence_order")
    private Integer sequenceOrder;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("total_initiatives")
    private Integer totalInitiatives;

    @JsonProperty("initiatives")
    private List<InitiativeResponse> initiatives;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
