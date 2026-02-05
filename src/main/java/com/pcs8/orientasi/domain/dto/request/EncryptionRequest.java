package com.pcs8.orientasi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptionRequest {

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Optional - used for decryption/verification endpoint.
     * Format: Base64-encoded AES encrypted password.
     */
    private String encryptedPassword;
}
