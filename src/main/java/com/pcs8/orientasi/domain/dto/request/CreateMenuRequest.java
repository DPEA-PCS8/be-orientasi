package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

    @JsonProperty("menu_code")
    @NotBlank(message = "Menu code is required")
    @Size(max = 100, message = "Menu code must not exceed 100 characters")
    private String menuCode;

    @JsonProperty("menu_name")
    @NotBlank(message = "Menu name is required")
    @Size(max = 100, message = "Menu name must not exceed 100 characters")
    private String menuName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @JsonProperty("parent_id")
    private UUID parentId;

    @JsonProperty("display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;
}