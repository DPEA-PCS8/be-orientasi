package com.pcs8.orientasi.domain.dto.rbsi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class InitiativeCreateRequest {

    @NotBlank(message = "Nama inisiatif tidak boleh kosong")
    @Size(max = 500, message = "Nama inisiatif maksimal 500 karakter")
    @JsonProperty("name")
    private String name;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    @JsonProperty("description")
    private String description;

    @JsonProperty("insert_after_sequence")
    private Integer insertAfterSequence;

    @JsonProperty("submit_date")
    private LocalDateTime submitDate;

    @JsonProperty("document_link")
    @Size(max = 500, message = "Link dokumen maksimal 500 karakter")
    private String documentLink;

    @JsonProperty("status")
    private String status;

    @JsonProperty("pksi_relation_id")
    private UUID pksiRelationId;
}
