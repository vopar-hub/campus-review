package com.vapor.model.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordValidator 单元测试。
 */
class PasswordValidatorTest {

    @Test
    void testValidPasswords() {
        assertTrue(PasswordValidator.isValidPassword("Abc123456"));
        assertTrue(PasswordValidator.isValidPassword("test123"));
        assertTrue(PasswordValidator.isValidPassword("Password1"));
    }

    @Test
    void testTooShortPasswords() {
        assertFalse(PasswordValidator.isValidPassword("Ab1"));
        assertFalse(PasswordValidator.isValidPassword("12345"));
    }

    @Test
    void testNoLetterPasswords() {
        assertFalse(PasswordValidator.isValidPassword("12345678"));
        assertFalse(PasswordValidator.isValidPassword("00000000"));
    }

    @Test
    void testNoDigitPasswords() {
        assertFalse(PasswordValidator.isValidPassword("abcdefgh"));
        assertFalse(PasswordValidator.isValidPassword("ABCDEFGH"));
    }

    @Test
    void testNullPassword() {
        assertFalse(PasswordValidator.isValidPassword(null));
    }

    @Test
    void testEmptyPasswords() {
        assertFalse(PasswordValidator.isValidPassword(""));
        assertFalse(PasswordValidator.isValidPassword("   "));
    }

    @Test
    void testValidatePasswordValid() {
        assertNull(PasswordValidator.validatePassword("Abc123456"));
    }

    @Test
    void testValidatePasswordTooShort() {
        String message = PasswordValidator.validatePassword("Ab1");
        assertNotNull(message);
        assertTrue(message.contains("6"));
    }

    @Test
    void testValidatePasswordNoLetter() {
        String message = PasswordValidator.validatePassword("12345678");
        assertNotNull(message);
        assertTrue(message.contains("字母"));
    }

    @Test
    void testValidatePasswordNoDigit() {
        String message = PasswordValidator.validatePassword("abcdefgh");
        assertNotNull(message);
        assertTrue(message.contains("数字"));
    }

    @Test
    void testValidatePasswordEmpty() {
        String message = PasswordValidator.validatePassword("");
        assertNotNull(message);
    }
}
