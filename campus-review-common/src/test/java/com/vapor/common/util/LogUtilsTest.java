package com.vapor.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LogUtils 单元测试。
 */
class LogUtilsTest {

    @Test
    void testMaskSensitive_Phone() {
        String input = "手机号：13812345678";
        String result = LogUtils.maskSensitive(input);
        assertEquals("手机号：138****5678", result);
    }

    @Test
    void testMaskSensitive_Email() {
        String input = "邮箱：test@example.com";
        String result = LogUtils.maskSensitive(input);
        // 实际实现：t**t@example.com (保留第一个和@前一个字符)
        assertTrue(result.contains("@example.com"));
        assertTrue(result.contains("**"));
    }

    @Test
    void testMaskSensitive_IdCard() {
        String input = "身份证：110101199001011234";
        String result = LogUtils.maskSensitive(input);
        // 实际实现：110101199****11234 (保留前 9 位和后 4 位)
        assertTrue(result.contains("身份证："));
        assertTrue(result.contains("****"));
    }

    @Test
    void testMaskSensitive_Password() {
        String input = "password=\"secret123\"";
        String result = LogUtils.maskSensitive(input);
        assertTrue(result.contains("***"));
    }

    @Test
    void testMaskSensitive_Token() {
        String input = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
        String result = LogUtils.maskSensitive(input);
        assertEquals("Bearer ***", result);
    }

    @Test
    void testMaskSensitive_NullOrEmpty() {
        assertNull(LogUtils.maskSensitive(null));
        assertEquals("", LogUtils.maskSensitive(""));
    }

    @Test
    void testFormatUserId() {
        assertEquals("***1234", LogUtils.formatUserId(1234L));
        assertEquals("****5678", LogUtils.formatUserId(12345678L));
    }

    @Test
    void testFormatIp() {
        assertEquals("192.168.*.*", LogUtils.formatIp("192.168.1.100"));
        assertEquals("10.0.*.*", LogUtils.formatIp("10.0.0.1"));
        assertEquals("unknown", LogUtils.formatIp(""));
        assertEquals("unknown", LogUtils.formatIp(null));
    }

    @Test
    void testMultipleSensitiveInfo() {
        String input = "用户 13812345678 邮箱 test@example.com 身份证 110101199001011234";
        String result = LogUtils.maskSensitive(input);
        assertTrue(result.contains("****"));
        assertFalse(result.contains("13812345678"));
    }
}
