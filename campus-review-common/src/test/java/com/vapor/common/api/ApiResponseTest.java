package com.vapor.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 单元测试。
 */
class ApiResponseTest {

    @Test
    void testOk_StaticMethod() {
        ApiResponse<String> response = ApiResponse.ok("OK");
        assertEquals(0, response.getCode());
        assertEquals("OK", response.getMessage());
        assertEquals("OK", response.getData());
        assertNull(response.getRequestId());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testOk_WithRequestId() {
        ApiResponse<Integer> response = ApiResponse.ok(42, "test-request-id");
        assertEquals(0, response.getCode());
        assertEquals("OK", response.getMessage());
        assertEquals(42, response.getData());
        assertEquals("test-request-id", response.getRequestId());
    }

    @Test
    void testOk_WithDataNull() {
        ApiResponse<Void> response = ApiResponse.ok(null);
        assertEquals(0, response.getCode());
        assertEquals("OK", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testFail_StaticMethod() {
        ApiResponse<Void> response = ApiResponse.fail(
            com.vapor.common.error.ErrorCode.BAD_REQUEST,
            "Custom error",
            "request-123"
        );
        assertEquals(40000, response.getCode());
        assertEquals("Custom error", response.getMessage());
        assertNull(response.getData());
        assertEquals("request-123", response.getRequestId());
    }

    @Test
    void testGetters() {
        ApiResponse<String> response = new ApiResponse<>(200, "Success", "Data", "req-1", 1234567890L);
        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals("Data", response.getData());
        assertEquals("req-1", response.getRequestId());
        assertEquals(1234567890L, response.getTimestamp());
    }
}
