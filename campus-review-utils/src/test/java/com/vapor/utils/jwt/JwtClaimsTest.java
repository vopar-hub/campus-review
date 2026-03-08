package com.vapor.utils.jwt;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtClaims 单元测试。
 */
class JwtClaimsTest {

    @Test
    void testConstructor() {
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        Instant expiresAt = Instant.now().plusSeconds(3600);

        JwtClaims claims = new JwtClaims(123L, roles, expiresAt);

        assertEquals(123L, claims.userId());
        assertEquals(roles, claims.roles());
        assertEquals(expiresAt, claims.expiresAt());
    }

    @Test
    void testParseRoles_NullOrEmpty() {
        assertTrue(JwtClaims.parseRoles(null).isEmpty());
        assertTrue(JwtClaims.parseRoles("").isEmpty());
        assertTrue(JwtClaims.parseRoles("   ").isEmpty());
    }

    @Test
    void testParseRoles_SingleRole() {
        Set<String> roles = JwtClaims.parseRoles("USER");
        assertEquals(1, roles.size());
        assertTrue(roles.contains("USER"));
    }

    @Test
    void testParseRoles_MultipleRoles() {
        Set<String> roles = JwtClaims.parseRoles("USER,ADMIN,EDITOR");
        assertEquals(3, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("EDITOR"));
    }

    @Test
    void testParseRoles_WithWhitespace() {
        Set<String> roles = JwtClaims.parseRoles(" USER , ADMIN , EDITOR ");
        assertEquals(3, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("EDITOR"));
    }

    @Test
    void testParseRoles_DuplicateRoles() {
        Set<String> roles = JwtClaims.parseRoles("USER,USER,ADMIN");
        assertEquals(2, roles.size()); // 去重
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }
}
