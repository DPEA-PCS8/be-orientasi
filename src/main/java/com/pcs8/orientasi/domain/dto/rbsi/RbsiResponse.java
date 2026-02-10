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
public class RbsiResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("periode")
    private String periode;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("total_programs")
    private Integer totalPrograms;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
