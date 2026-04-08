package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Base class for changelog responses.
 * Used by PksiChangelogResponse and Fs2ChangelogResponse to reduce code duplication.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseChangelogResponse {

    protected String id;

    @JsonProperty("field_name")
    protected String fieldName;

    @JsonProperty("field_label")
    protected String fieldLabel;

    @JsonProperty("old_value")
    protected String oldValue;

    @JsonProperty("new_value")
    protected String newValue;

    @JsonProperty("updated_by")
    protected String updatedBy;

    @JsonProperty("updated_by_name")
    protected String updatedByName;

    @JsonProperty("updated_at")
    protected LocalDateTime updatedAt;
}
