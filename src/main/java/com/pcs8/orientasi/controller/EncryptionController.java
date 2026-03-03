package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.request.EncryptionRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.EncryptionResponse;
import com.pcs8.orientasi.service.PasswordEncryptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crypto")
@RequiredArgsConstructor
public class EncryptionController {

    private static final Logger log = LoggerFactory.getLogger(EncryptionController.class);

    private final PasswordEncryptionService passwordEncryptionService;

    /**
     * Endpoint untuk encrypt password dengan RSA (development and testing only).
     * 
     * Request body: EncryptionRequest with password field
     * Response: EncryptionResponse with original and encrypted password
     */
    @PostMapping("/encrypt")
    public ResponseEntity<BaseResponse> encryptPassword(@Valid @RequestBody EncryptionRequest request) {
        log.info("Encrypt password with RSA (development/testing)");

        try {
            String encryptedPassword = passwordEncryptionService.encrypt(request.getPassword());

            EncryptionResponse response = EncryptionResponse.builder()
                    .password(request.getPassword())
                    .encryptedPassword(encryptedPassword)
                    .build();

            return ResponseEntity.ok(new BaseResponse(200, "Password encrypted successfully", response));

        } catch (RuntimeException e) {
            log.error("Error encrypting password: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BaseResponse(400, "Error encrypting password: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint untuk decrypt password dengan RSA (development and testing only).
     * 
     * Request body: EncryptionRequest with encrypted_password field
     * Response: DecryptResponse with decrypted password
     */
    @PostMapping("/decrypt")
    public ResponseEntity<BaseResponse> decryptPassword(@Valid @RequestBody EncryptionRequest request) {
        log.info("Decrypt password with RSA (development/testing)");

        try {
            if (request.getEncryptedPassword() == null || request.getEncryptedPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse(400, "Encrypted password is required for decryption", null));
            }

            String decryptedPassword = passwordEncryptionService.decrypt(request.getEncryptedPassword());

            EncryptionResponse.DecryptResponse response = EncryptionResponse.DecryptResponse.builder()
                    .decryptedPassword(decryptedPassword)
                    .build();

            return ResponseEntity.ok(new BaseResponse(200, "Password decrypted successfully", response));

        } catch (RuntimeException e) {
            log.error("Error decrypting password: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BaseResponse(400, "Error decrypting password: " + e.getMessage(), null));
        }
    }


}
