package com.pcs8.orientasi.domain.dto.rbsi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProgramCreateRequest {

    @NotNull(message = "RBSI ID tidak boleh kosong")
    @JsonProperty("rbsi_id")
    private UUID rbsiId;

    @NotBlank(message = "Nama program tidak boleh kosong")
    @Size(max = 500, message = "Nama program maksimal 500 karakter")
    @JsonProperty("name")
    private String name;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    @JsonProperty("description")
    private String description;

    @NotNull(message = "Tahun versi tidak boleh kosong")
    @JsonProperty("year_version")
    private Integer yearVersion;

    @JsonProperty("insert_after_sequence")
    private Integer insertAfterSequence;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("status")
    private String status;

    @Valid
    @JsonProperty("initiatives")
    private List<InitiativeCreateRequest> initiatives;
}
