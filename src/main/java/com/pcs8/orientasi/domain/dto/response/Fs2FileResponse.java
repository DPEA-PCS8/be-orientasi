package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Fs2FileResponse {
    private UUID id;
    private UUID fs2Id;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String blobUrl;
    private String fileType; // ND, FS2, CD, FS2A, FS2B, F45, F46, NDBA
    private LocalDateTime createdAt;
    private Integer version;
    private UUID fileGroupId;
    private String displayName;
    private boolean isLatestVersion;
}
