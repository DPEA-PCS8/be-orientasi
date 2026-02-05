package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("user_info")
    private UserInfo userInfo;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        
        private String uuid;
        
        private String username;

        @JsonProperty("full_name")
        private String fullName;
        
        // Keep displayName for LDAP compatibility
        @JsonProperty("display_name")
        private String displayName;

        private String email;

        private String department;

        private String title;

        @JsonProperty("distinguished_name")
        private String distinguishedName;
        
        @JsonProperty("last_login_at")
        private LocalDateTime lastLoginAt;
    }
}