package com.syedsadiquh.coreservice.journal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class JournalEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BIT_LENGTH = 128;
    private static final String ENC_KEY = "_enc";

    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public JournalEncryptionService(
            @Value("${journal.encryption.key}") String base64Key,
            ObjectMapper objectMapper) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("journal.encryption.key must be a base64-encoded 32-byte (256-bit) AES key");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.objectMapper = objectMapper;
    }

    public String encryptString(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BIT_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, combined, IV_LENGTH, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a string encrypted by encryptString. Falls back to the raw value for
     * unencrypted legacy data that predates encryption rollout.
     */
    public String decryptString(String value) {
        if (value == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(value);
            if (combined.length <= IV_LENGTH) return value;
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BIT_LENGTH, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Decryption failed for value, returning raw (likely legacy unencrypted data)");
            return value;
        }
    }

    public Map<String, Object> encryptMap(Map<String, Object> map) {
        if (map == null) return null;
        try {
            String json = objectMapper.writeValueAsString(map);
            return Map.of(ENC_KEY, encryptString(json));
        } catch (Exception e) {
            throw new RuntimeException("Map encryption failed", e);
        }
    }

    /**
     * Decrypts a map encrypted by encryptMap. Maps without the _enc key are returned
     * as-is (legacy unencrypted data).
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> decryptMap(Map<String, Object> map) {
        if (map == null) return null;
        if (!map.containsKey(ENC_KEY)) return map;
        try {
            String decryptedJson = decryptString((String) map.get(ENC_KEY));
            return objectMapper.readValue(decryptedJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Map decryption failed, returning raw map (likely legacy unencrypted data)");
            return map;
        }
    }
}
