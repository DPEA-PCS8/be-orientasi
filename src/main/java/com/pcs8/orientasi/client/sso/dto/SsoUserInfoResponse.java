package com.pcs8.orientasi.client.sso.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response from the SSO {@code /connect/userinfo} endpoint.
 *
 * <p>Claim mapping for the app: {@code sub -> username}, {@code name -> full name/display name},
 * {@code email -> email}, {@code jabatan -> title}, {@code organization -> department}.
 * {@code user_type} is NOT an app role (roles are resolved from the BE DB).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SsoUserInfoResponse {

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("jabatan")
    private String jabatan;

    @JsonProperty("user_type")
    private String userType;
}
