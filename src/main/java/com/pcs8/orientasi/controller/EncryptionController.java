package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.request.EncryptionRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.EncryptionResponse;
import com.pcs8.orientasi.service.impl.PasswordEncryptionServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crypto")
public class EncryptionController {

    private static final Logger log = LoggerFactory.getLogger(EncryptionController.class);

    private final PasswordEncryptionServiceImpl passwordEncryptionService;

    public EncryptionController(PasswordEncryptionServiceImpl passwordEncryptionService) {
        this.passwordEncryptionService = passwordEncryptionService;
    }

    /**
     * Endpoint untuk encrypt password dengan AES.
     * Digunakan untuk development & testing - frontend bisa encrypt password di sini.
     * 
     * Request:
     * {
     *   "password": "mypassword123"
     * }
     * 
     * Response:
     * {
     *   "status": 200,
     *   "message": "Password encrypted successfully",
     *   "data": {
     *     "password": "mypassword123",
     *     "encrypted_password": "U2FsdGVkX1..."
     *   }
     * }
     */
    @PostMapping("/encrypt")
    public ResponseEntity<BaseResponse> encryptPassword(@Valid @RequestBody EncryptionRequest request) {
        log.info("Encrypt password with AES (development/testing)");

        try {
            String encryptedPassword = passwordEncryptionService.encrypt(request.getPassword());

            EncryptionResponse response = EncryptionResponse.builder()
                    .password(request.getPassword())
                    .encryptedPassword(encryptedPassword)
                    .build();

            return ResponseEntity.ok(new BaseResponse(200, "Password encrypted successfully", response));

        } catch (Exception e) {
            log.error("Error encrypting password: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BaseResponse(400, "Error encrypting password: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint untuk decrypt password dengan AES.
     * Digunakan untuk development & testing - verify encryption/decryption.
     * 
     * Request:
     * {
     *   "encrypted_password": "U2FsdGVkX1..."
     * }
     * 
     * Response:
     * {
     *   "status": 200,
     *   "message": "Password decrypted successfully",
     *   "data": {
     *     "decrypted_password": "mypassword123"
     *   }
     * }
     */
    @PostMapping("/decrypt")
    public ResponseEntity<BaseResponse> decryptPassword(@Valid @RequestBody EncryptionRequest request) {
        log.info("Decrypt password with AES (development/testing)");

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

        } catch (Exception e) {
            log.error("Error decrypting password: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BaseResponse(400, "Error decrypting password: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint untuk generate AES key (testing/development only!).
     * Gunakan ini untuk generate configuration key yang baru.
     * 
     * Response:
     * {
     *   "status": 200,
     *   "message": "AES key generated successfully",
     *   "data": {
     *     "key": "U2FsdGVkX1..."
     *   }
     * }
     */
    @GetMapping("/generate-key")
    public ResponseEntity<BaseResponse> generateAesKey() {
        log.warn("Generating new AES key - DEVELOPMENT/TESTING ONLY!");

        try {
            passwordEncryptionService.printGeneratedKey();

            return ResponseEntity.ok(new BaseResponse(200, 
                    "AES key generated. Check logs for key value. Update application.yaml with aes.encryption.key=<generated_key>", 
                    null));

        } catch (Exception e) {
            log.error("Error generating AES key: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BaseResponse(400, "Error generating AES key: " + e.getMessage(), null));
        }
    }
}
