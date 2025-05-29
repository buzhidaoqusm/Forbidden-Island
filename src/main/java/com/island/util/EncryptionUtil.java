package com.island.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Encryption utility class that provides message encryption and decryption functionality
 */
public class EncryptionUtil {
    /** The encryption algorithm to be used (AES) */
    private static final String ALGORITHM = "AES";
    
    /** The encryption key string (should be stored more securely in production) */
    private static final String KEY = "ForbiddenIsland2025"; // Fixed key, should be stored more securely in production
    
    /** The secret key instance used for encryption/decryption */
    private static SecretKey secretKey;

    static {
        try {
            // Initialize with fixed key
            byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
            // Ensure key length is 16 bytes (128 bits)
            byte[] paddedKey = new byte[16];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, paddedKey.length));
            secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypts a message
     * @param message The message to encrypt
     * @return Base64 encoded encrypted string
     */
    public static String encrypt(String message) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return message; // Return original message if encryption fails
        }
    }

    /**
     * Decrypts a message
     * @param encryptedMessage The Base64 encoded encrypted string
     * @return Decrypted message
     */
    public static String decrypt(String encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedMessage; // Return original message if decryption fails
        }
    }
} 