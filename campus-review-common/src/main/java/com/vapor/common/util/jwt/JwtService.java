package com.vapor.common.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 签发与校验服务。
 *
 * 负责签发包含 userId 与 roles 的 token，并在解析时校验签名与过期时间。
 */
public class JwtService {
    private final SecretKey secretKey;
    private final Duration ttl;

    /**
     * 构造 JWT 服务。
     *
     * @param secret 签名密钥
     * @param ttl token 有效期
     */
    public JwtService(String secret, Duration ttl) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = ttl;
    }

    /**
     * 签发 token。
     *
     * @param userId 用户 ID
     * @param roles 角色集合
     * @return JWT 字符串
     */
    public String issue(Long userId, Set<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        String rolesCsv = roles == null ? "" : roles.stream().sorted().collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("roles", rolesCsv)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并校验 token。
     *
     * @param token JWT 字符串
     * @return 解析后的声明
     */
    public JwtClaims parseAndValidate(String token) {
        Jws<Claims> jws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        Claims claims = jws.getPayload();
        Long userId = Long.parseLong(claims.getSubject());
        String rolesCsv = claims.get("roles", String.class);
        Set<String> roles = JwtClaims.parseRoles(rolesCsv);
        Instant expiresAt = claims.getExpiration().toInstant();
        return new JwtClaims(userId, roles, expiresAt);
    }
}
