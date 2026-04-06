package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PksiChangelogResponse {

    private String id;

    @JsonProperty("field_name")
    private String fieldName;

    @JsonProperty("field_label")
    private String fieldLabel;

    @JsonProperty("old_value")
    private String oldValue;

    @JsonProperty("new_value")
    private String newValue;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("updated_by_name")
    private String updatedByName;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
