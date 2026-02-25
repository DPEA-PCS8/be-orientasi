package com.pcs8.orientasi.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private UUID id;
    private String menuCode;
    private String menuName;
    private String description;
    private UUID parentId;
    private String parentName;
    private Integer displayOrder;
    private Boolean isActive;
    private List<MenuResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}