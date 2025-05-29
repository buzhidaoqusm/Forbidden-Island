package com.forbiddenisland.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Base64;

/**
 * Test class for EncryptionUtil functionality
 */
class EncryptionUtilTest {

    /**
     * Tests basic encryption and decryption of a simple message
     */
    @Test
    void testBasicEncryptionDecryption() {
        String originalMessage = "Hello World";
        String encrypted = EncryptionUtil.encrypt(originalMessage);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertNotNull(encrypted);
        assertNotEquals(originalMessage, encrypted);
        assertEquals(originalMessage, decrypted);
    }

    /**
     * Tests encryption and decryption of empty string
     */
    @Test
    void testEmptyString() {
        String originalMessage = "";
        String encrypted = EncryptionUtil.encrypt(originalMessage);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertNotNull(encrypted);
        assertEquals(originalMessage, decrypted);
    }

    /**
     * Tests encryption and decryption of special characters
     */
    @Test
    void testSpecialCharacters() {
        String originalMessage = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String encrypted = EncryptionUtil.encrypt(originalMessage);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertNotNull(encrypted);
        assertNotEquals(originalMessage, encrypted);
        assertEquals(originalMessage, decrypted);
    }

    /**
     * Tests encryption and decryption of Unicode characters
     */
    @Test
    void testUnicodeCharacters() {
        String originalMessage = "你好世界🌍";
        String encrypted = EncryptionUtil.encrypt(originalMessage);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertNotNull(encrypted);
        assertNotEquals(originalMessage, encrypted);
        assertEquals(originalMessage, decrypted);
    }

    /**
     * Tests encryption and decryption of long messages
     */
    @Test
    void testLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("Long message test ");
        }
        
        String originalMessage = longMessage.toString();
        String encrypted = EncryptionUtil.encrypt(originalMessage);
        String decrypted = EncryptionUtil.decrypt(encrypted);
        
        assertNotNull(encrypted);
        assertNotEquals(originalMessage, encrypted);
        assertEquals(originalMessage, decrypted);
    }

    /**
     * Tests that encrypted output is valid Base64
     */
    @Test
    void testEncryptedOutputFormat() {
        String originalMessage = "Test message";
        String encrypted = EncryptionUtil.encrypt(originalMessage);
        
        // Verify the encrypted output is valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encrypted));
    }

    /**
     * Tests that different messages produce different encrypted outputs
     */
    @Test
    void testDifferentMessages() {
        String message1 = "First message";
        String message2 = "Second message";
        
        String encrypted1 = EncryptionUtil.encrypt(message1);
        String encrypted2 = EncryptionUtil.encrypt(message2);
        
        assertNotEquals(encrypted1, encrypted2);
    }

    /**
     * Tests that same message produces same encrypted output
     */
    @Test
    void testConsistentEncryption() {
        String message = "Test message";
        
        String encrypted1 = EncryptionUtil.encrypt(message);
        String encrypted2 = EncryptionUtil.encrypt(message);
        
        assertEquals(encrypted1, encrypted2);
    }

    /**
     * Tests handling of null input
     */
    @Test
        void testNullInput() {
            // Encryption null: should return null (not thrown)
            String encrypted = assertDoesNotThrow(() -> EncryptionUtil.encrypt(null));
            assertNull(encrypted); // If the method returns null

            // Decrypting null: should also return null
            String decrypted = assertDoesNotThrow(() -> EncryptionUtil.decrypt(null));
            assertNull(decrypted);
    }


    /**
     * Tests decryption of invalid encrypted message
     */
    @Test
    void testInvalidEncryptedMessage() {
        // Test with invalid Base64 input (contains space)
        String invalidBase64 = "Invalid Base64 Input";
        String decrypted = EncryptionUtil.decrypt(invalidBase64);
        // Should return original message when decryption fails
        assertEquals(invalidBase64, decrypted);

        // Test with valid Base64 but invalid encrypted content
        String validBase64 = "SGVsbG8="; // Base64 encoded "Hello"
        decrypted = EncryptionUtil.decrypt(validBase64);
        assertEquals(validBase64, decrypted); // Should return original input when decryption fails
    }

    /**
     * Tests that decryption gracefully handles invalid input
     */
    @Test
    void testDecryptionErrorHandling() {
        // Test with empty string
        assertEquals("", EncryptionUtil.decrypt(""));
        
        // Test with various invalid inputs
        String[] invalidInputs = {
            "Not Base64",
            "Invalid-Characters!@#",
            "12345", // Too short
            "VeryLongInvalidString=====" // Invalid padding
        };
        
        for (String input : invalidInputs) {
            String result = EncryptionUtil.decrypt(input);
            assertEquals(input, result, "Should return original input for invalid Base64: " + input);
        }
    }
} 