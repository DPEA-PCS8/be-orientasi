package com.pcs8.orientasi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /auth/sso/exchange}: the authorization {@code code}
 * and {@code state} relayed by the FE callback.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoExchangeRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String state;
}
