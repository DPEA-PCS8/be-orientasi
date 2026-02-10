package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @JsonProperty("program_number")
    private String programNumber;

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

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("total_initiatives")
    private Integer totalInitiatives;

    @JsonProperty("initiatives")
    private List<InitiativeResponse> initiatives;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
