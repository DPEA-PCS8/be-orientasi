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
public class PksiFileResponse {
    private UUID id;
    private UUID pksiId;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String blobUrl;
    private String fileType; // T01 = Rencana PKSI, T11 = Spesifikasi Kebutuhan
    private LocalDateTime createdAt;
}
