package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class BaseFileResponse {
    private UUID id;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String blobUrl;
    private String fileType;
    private LocalDateTime createdAt;
    private Integer version;
    private UUID fileGroupId;
    private String displayName;
    private boolean isLatestVersion;
    private LocalDate tanggalDokumen;
}
