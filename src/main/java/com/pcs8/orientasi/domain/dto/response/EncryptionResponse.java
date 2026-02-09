package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EncryptionResponse {

    private String password;

    @JsonProperty("encrypted_password")
    private String encryptedPassword;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DecryptResponse {

        @JsonProperty("decrypted_password")
        private String decryptedPassword;
    }
}
