package com.pcs8.orientasi.domain.dto.response;

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
public class PksiFileResponse {
    private UUID id;
    private UUID pksiId;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String blobUrl;
    private LocalDateTime createdAt;
}
