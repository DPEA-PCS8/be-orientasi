package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiativeCreateRequest {

    @NotNull(message = "Program ID is required")
    @JsonProperty("program_id")
    private UUID programId;

    @NotBlank(message = "Initiative name is required")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Year version is required")
    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("status")
    private String status;

    @JsonProperty("link_dokumen")
    private String linkDokumen;

    @JsonProperty("tanggal_submit")
    private String tanggalSubmit;

    @JsonProperty("insert_at_position")
    private Integer insertAtPosition;
}
