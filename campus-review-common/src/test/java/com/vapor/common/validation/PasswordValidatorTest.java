package com.vapor.common.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordValidator 单元测试。
 */
class PasswordValidatorTest {

    @Test
    void testIsValidPassword_Valid() {
        assertTrue(PasswordValidator.isValidPassword("Abc123456"));
        assertTrue(PasswordValidator.isValidPassword("test123"));
        assertTrue(PasswordValidator.isValidPassword("Password1"));
    }

    @Test
    void testIsValidPassword_TooShort() {
        assertFalse(PasswordValidator.isValidPassword("Ab1"));
        assertFalse(PasswordValidator.isValidPassword("12345"));
    }

    @Test
    void testIsValidPassword_NoLetter() {
        assertFalse(PasswordValidator.isValidPassword("12345678"));
        assertFalse(PasswordValidator.isValidPassword("00000000"));
    }

    @Test
    void testIsValidPassword_NoDigit() {
        assertFalse(PasswordValidator.isValidPassword("abcdefgh"));
        assertFalse(PasswordValidator.isValidPassword("ABCDEFGH"));
    }

    @Test
    void testIsValidPassword_Null() {
        assertFalse(PasswordValidator.isValidPassword(null));
    }

    @Test
    void testIsValidPassword_Empty() {
        assertFalse(PasswordValidator.isValidPassword(""));
        assertFalse(PasswordValidator.isValidPassword("   "));
    }

    @Test
    void testValidatePassword_Valid() {
        assertNull(PasswordValidator.validatePassword("Abc123456"));
    }

    @Test
    void testValidatePassword_TooShort() {
        String message = PasswordValidator.validatePassword("Ab1");
        assertNotNull(message);
        assertTrue(message.contains("长度"));
    }

    @Test
    void testValidatePassword_NoLetter() {
        String message = PasswordValidator.validatePassword("12345678");
        assertNotNull(message);
        assertTrue(message.contains("字母"));
    }

    @Test
    void testValidatePassword_NoDigit() {
        String message = PasswordValidator.validatePassword("abcdefgh");
        assertNotNull(message);
        assertTrue(message.contains("数字"));
    }

    @Test
    void testValidatePassword_Empty() {
        String message = PasswordValidator.validatePassword("");
        assertNotNull(message);
        assertTrue(message.contains("为空"));
    }
}
