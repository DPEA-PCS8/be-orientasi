package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO untuk AuditLog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;

    @JsonProperty("entity_name")
    private String entityName;

    @JsonProperty("entity_id")
    private UUID entityId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("old_value")
    private Object oldValue;

    @JsonProperty("new_value")
    private Object newValue;

    @JsonProperty("changed_fields")
    private Object changedFields;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
