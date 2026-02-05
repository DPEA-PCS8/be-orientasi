package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.service.PasswordEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Password Encryption Service menggunakan AES (Advanced Encryption Standard).
 * 
 * AES adalah symmetric encryption yang bisa di-encrypt dan di-decrypt.
 * 
 * Flow:
 * 1. Frontend encrypt password dengan AES sebelum kirim ke backend
 * 2. Backend decrypt AES-encrypted password untuk dapat plaintext
 * 3. Plaintext password dikirim ke LDAP untuk authentication
 */
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(PasswordEncryptionServiceImpl.class);
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256; // 256-bit key
    
    private final SecretKey secretKey;

    public PasswordEncryptionServiceImpl(@Value("${aes.encryption.key:}") String encryptionKeyFromConfig) {
        // Initialize AES key
        // Jika dari config kosong, generate random key (untuk testing only!)
        if (encryptionKeyFromConfig == null || encryptionKeyFromConfig.isEmpty()) {
            log.warn("AES encryption key not found in config, generating random key for testing");
            this.secretKey = generateRandomKey();
        } else {
            this.secretKey = decodeKeyFromString(encryptionKeyFromConfig);
        }
    }

    @Override
    public String encrypt(String rawPassword) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(rawPassword.getBytes(StandardCharsets.UTF_8));
            String encryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);
            
            log.debug("Password encrypted successfully");
            return encryptedPassword;
        } catch (Exception e) {
            log.error("Error encrypting password: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt password", e);
        }
    }

    @Override
    public String decrypt(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            String decryptedPassword = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            log.debug("Password decrypted successfully");
            return decryptedPassword;
        } catch (IllegalArgumentException e) {
            log.warn("Password is not valid Base64 or AES encrypted, using as plain text");
            return encryptedPassword;
        } catch (Exception e) {
            log.error("Error decrypting password: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt password", e);
        }
    }

    /**
     * Generate random AES key (256-bit).
     * Hanya untuk testing/development!
     */
    private SecretKey generateRandomKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            SecretKey key = keyGen.generateKey();
            log.info("Generated random AES key (testing only): {}", encodeKeyToString(key));
            return key;
        } catch (Exception e) {
            log.error("Error generating random key: {}", e.getMessage());
            throw new RuntimeException("Failed to generate random key", e);
        }
    }

    /**
     * Decode AES key dari String format (Base64).
     * Format: Base64-encoded bytes dari SecretKey
     */
    private SecretKey decodeKeyFromString(String encodedKey) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            if (decodedKey.length != KEY_SIZE / 8) {
                log.warn("Key size mismatch: expected {} bytes, got {}", KEY_SIZE / 8, decodedKey.length);
            }
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
        } catch (IllegalArgumentException e) {
            log.error("Invalid AES key format: {}", e.getMessage());
            throw new RuntimeException("Failed to decode AES key from config", e);
        }
    }

    /**
     * Encode AES key ke String format (Base64).
     * Gunakan ini untuk generate config value dari generated key.
     */
    public String encodeKeyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Generate dan print AES key untuk dimasukkan ke config.
     * Gunakan untuk setup awal.
     */
    public void printGeneratedKey() {
        SecretKey newKey = generateRandomKey();
        String encodedKey = encodeKeyToString(newKey);
        log.info("=== Generated AES Key (masukkan ke application.yaml) ===");
        log.info("aes.encryption.key={}", encodedKey);
        log.info("=== End of Generated Key ===");
    }
}