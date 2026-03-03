package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.service.PasswordEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Password Encryption Service menggunakan RSA.
 * Public key untuk encrypt, private key untuk decrypt.
 */
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(PasswordEncryptionServiceImpl.class);
    private static final String ALGORITHM = "RSA";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int KEY_SIZE = 2048;
    
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${rsa.encryption.private-key}")
    private String privateKeyFromConfig;

    @Value("${rsa.encryption.public-key}")
    private String publicKeyFromConfig;

    @PostConstruct
    public void init() {
        log.info("Initializing RSA encryption service");
        try {
            // Try to decode keys from config
            this.privateKey = decodePrivateKeyFromString(privateKeyFromConfig);
            this.publicKey = decodePublicKeyFromString(publicKeyFromConfig);
            log.info("RSA keys loaded from config successfully");
        } catch (GeneralSecurityException e) {
            log.warn("Failed to load RSA keys from config: {}. Generating new keys...", e.getMessage());
            // Fallback: generate new key pair
            generateAndSetNewKeyPair();
        }
    }

    /**
     * Generate new RSA key pair dan set ke instance variables.
     */
    private void generateAndSetNewKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            
            log.warn("=== NEW RSA KEY PAIR GENERATED ===");
            log.warn("Keys generated successfully. Please configure them securely in application.yaml");
            log.warn("=== END OF GENERATED KEYS ===");
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate RSA key pair", e);
            throw new RuntimeException("Cannot initialize RSA encryption service", e);
        }
    }

    @Override
    public String encrypt(String rawPassword) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] encryptedBytes = cipher.doFinal(rawPassword.getBytes(StandardCharsets.UTF_8));
            String encryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);
            
            log.debug("Password encrypted successfully with RSA public key");
            return encryptedPassword;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            log.error("Error encrypting password", e);
            throw new RuntimeException("Failed to encrypt password with RSA", e);
        }
    }

    @Override
    public String decrypt(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            String decryptedPassword = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            log.debug("Password decrypted successfully with RSA private key");
            return decryptedPassword;
        } catch (IllegalArgumentException e) {
            log.warn("Password is not valid Base64 or RSA encrypted, using as plain text");
            return encryptedPassword;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            log.error("Error decrypting password", e);
            throw new RuntimeException("Failed to decrypt password with RSA", e);
        }
    }



    /**
     * Decode RSA private key dari String format (Base64).
     */
    private PrivateKey decodePrivateKeyFromString(String encodedKey) throws GeneralSecurityException {
        if (encodedKey == null || encodedKey.trim().isEmpty()) {
            throw new InvalidKeyException("Private key config is empty");
        }
        
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (IllegalArgumentException e) {
            log.error("Invalid RSA private key Base64 format: {}", e.getMessage());
            throw new InvalidKeyException("Invalid private key format - not valid Base64", e);
        }
    }

    /**
     * Decode RSA public key dari String format (Base64).
     */
    private PublicKey decodePublicKeyFromString(String encodedKey) throws GeneralSecurityException {
        if (encodedKey == null || encodedKey.trim().isEmpty()) {
            throw new InvalidKeyException("Public key config is empty");
        }
        
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (IllegalArgumentException e) {
            log.error("Invalid RSA public key Base64 format: {}", e.getMessage());
            throw new InvalidKeyException("Invalid public key format - not valid Base64", e);
        }
    }



    /**
     * Get public key untuk dikirim ke frontend.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}