package com.vapor.utils.jwt;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtService 单元测试。
 */
class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-for-jwt-service-min-32-chars";
    private static final Duration TEST_TTL = Duration.ofHours(1);

    @Test
    void testIssueAndParse() {
        JwtService jwtService = new JwtService(TEST_SECRET, TEST_TTL);
        Long userId = 12345L;
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        roles.add("ADMIN");

        String token = jwtService.issue(userId, roles);
        assertNotNull(token);
        assertTrue(token.length() > 0);

        JwtClaims claims = jwtService.parseAndValidate(token);
        assertEquals(userId, claims.userId());
        assertTrue(claims.roles().contains("USER"));
        assertTrue(claims.roles().contains("ADMIN"));
    }

    @Test
    void testIssueWithNullRoles() {
        JwtService jwtService = new JwtService(TEST_SECRET, TEST_TTL);
        String token = jwtService.issue(123L, null);
        assertNotNull(token);

        JwtClaims claims = jwtService.parseAndValidate(token);
        assertEquals(123L, claims.userId());
        assertTrue(claims.roles().isEmpty());
    }

    @Test
    void testIssueWithEmptyRoles() {
        JwtService jwtService = new JwtService(TEST_SECRET, TEST_TTL);
        String token = jwtService.issue(456L, new HashSet<>());
        assertNotNull(token);

        JwtClaims claims = jwtService.parseAndValidate(token);
        assertEquals(456L, claims.userId());
        assertTrue(claims.roles().isEmpty());
    }

    @Test
    void testParseInvalidToken() {
        JwtService jwtService = new JwtService(TEST_SECRET, TEST_TTL);
        assertThrows(Exception.class, () -> {
            jwtService.parseAndValidate("invalid.token.here");
        });
    }

    @Test
    void testParseTamperedToken() {
        JwtService jwtService = new JwtService(TEST_SECRET, TEST_TTL);
        String token = jwtService.issue(789L, Set.of("USER"));

        // 篡改 token
        String tampered = token.substring(0, token.length() - 5) + "tampered";

        assertThrows(Exception.class, () -> {
            jwtService.parseAndValidate(tampered);
        });
    }

    @Test
    void testDifferentSecrets() {
        JwtService service1 = new JwtService(TEST_SECRET, TEST_TTL);
        JwtService service2 = new JwtService("different-secret-key-min-32-chars-long", TEST_TTL);

        String token = service1.issue(111L, Set.of("USER"));

        // 使用不同密钥的服务解析应该失败
        assertThrows(Exception.class, () -> {
            service2.parseAndValidate(token);
        });
    }
}
