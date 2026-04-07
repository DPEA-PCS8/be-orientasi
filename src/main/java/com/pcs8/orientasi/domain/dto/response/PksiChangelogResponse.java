package com.pcs8.orientasi.domain.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Response for PKSI document changelog.
 * Extends BaseChangelogResponse to reduce code duplication.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PksiChangelogResponse extends BaseChangelogResponse {
    // All fields are inherited from BaseChangelogResponse
    // No additional fields needed
}
