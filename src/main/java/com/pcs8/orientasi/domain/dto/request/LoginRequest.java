package com.pcs8.orientasi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Password dalam format AES-encrypted (Base64-encoded).
     *
     * Flow:
     * 1. Frontend: plaintext_password di-encrypt dengan AES
     * 2. Frontend: encrypted password di-encode dengan Base64
     * 3. Frontend: kirim { username, password: encrypted_base64 } ke backend
     * 4. Backend: decode Base64 dan decrypt AES untuk dapat plaintext
     * 5. Backend: kirim plaintext ke LDAP untuk authentication
     *
     * Untuk generate encrypted password, gunakan endpoint: POST /api/crypto/encrypt
     */
    @NotBlank(message = "Password is required")
    private String password;
}