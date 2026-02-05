package com.pcs8.orientasi.service;

public interface PasswordEncryptionService {

    /**
     * Encrypt password dengan AES.
     * Digunakan untuk testing/development - generate encrypted password.
     */
    String encrypt(String rawPassword);

    /**
     * Decrypt AES-encrypted password.
     * Digunakan sebelum kirim ke LDAP untuk authentication.
     */
    String decrypt(String encryptedPassword);
}