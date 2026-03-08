package com.vapor.utils.validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CampusAccountValidator 单元测试。
 */
class CampusAccountValidatorTest {

    @Test
    void testIsValidEmail_Valid() {
        assertTrue(CampusAccountValidator.isValidEmail("test@campus.edu"));
        assertTrue(CampusAccountValidator.isValidEmail("user@example.com"));
        assertTrue(CampusAccountValidator.isValidEmail("test.user@campus.edu.cn"));
    }

    @Test
    void testIsValidEmail_Invalid() {
        assertFalse(CampusAccountValidator.isValidEmail("invalid"));
        assertFalse(CampusAccountValidator.isValidEmail("@example.com"));
        assertFalse(CampusAccountValidator.isValidEmail("test@"));
        assertFalse(CampusAccountValidator.isValidEmail("test@example"));
    }

    @Test
    void testIsValidEmail_NullOrEmpty() {
        assertFalse(CampusAccountValidator.isValidEmail(null));
        assertFalse(CampusAccountValidator.isValidEmail(""));
    }

    @Test
    void testIsValidStudentNo_Valid() {
        assertTrue(CampusAccountValidator.isValidStudentNo("20250001"));
        assertTrue(CampusAccountValidator.isValidStudentNo("20241234"));
        assertTrue(CampusAccountValidator.isValidStudentNo("20235678"));
    }

    @Test
    void testIsValidStudentNo_Invalid() {
        assertFalse(CampusAccountValidator.isValidStudentNo("123"));
        assertFalse(CampusAccountValidator.isValidStudentNo("12345"));
        assertFalse(CampusAccountValidator.isValidStudentNo("abcd1234"));
    }

    @Test
    void testIsValidStudentNo_NullOrEmpty() {
        assertFalse(CampusAccountValidator.isValidStudentNo(null));
        assertFalse(CampusAccountValidator.isValidStudentNo(""));
    }
}
