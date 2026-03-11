package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangelogInfo {

    private UUID id;

    @JsonProperty("tanggal_perubahan")
    private LocalDate tanggalPerubahan;

    @JsonProperty("keterangan")
    private String keterangan;

    @JsonProperty("perubahan_detail")
    private String perubahanDetail;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
