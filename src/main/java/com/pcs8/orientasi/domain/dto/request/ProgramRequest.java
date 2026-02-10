package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRequest {

    @NotNull(message = "RBSI ID is required")
    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @NotBlank(message = "Program name is required")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Year version is required")
    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("insert_at_position")
    private Integer insertAtPosition;

    @JsonProperty("status")
    private String status;

    @JsonProperty("start_date")
    private String startDate;

    @Valid
    @JsonProperty("initiatives")
    private List<InitiativeRequest> initiatives;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiativeRequest {

        @NotBlank(message = "Initiative name is required")
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("status")
        private String status;

        @JsonProperty("link_dokumen")
        private String linkDokumen;

        @JsonProperty("tanggal_submit")
        private String tanggalSubmit;

        @JsonProperty("insert_at_position")
        private Integer insertAtPosition;
    }
}
