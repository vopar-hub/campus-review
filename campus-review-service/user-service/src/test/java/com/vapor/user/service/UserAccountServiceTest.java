package com.vapor.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.model.auth.LoginRequest;
import com.vapor.model.auth.LoginResponse;
import com.vapor.model.auth.RegisterRequest;
import com.vapor.model.user.UserDTO;
import com.vapor.user.entity.UserEntity;
import com.vapor.user.mapper.UserMapper;
import com.vapor.utils.jwt.JwtClaims;
import com.vapor.utils.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserAccountService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserAccountService userAccountService;

    private UserEntity testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setEmail("test@campus.edu");
        testUser.setStudentNo("20250001");
        testUser.setNickname("test");
        testUser.setPasswordHash("encodedPassword");
        testUser.setRoles("USER");
        testUser.setBanned(false);
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());

        registerRequest = new RegisterRequest("test@campus.edu", "20250001", "password123", "test");
        loginRequest = new LoginRequest("test@campus.edu", "password123");
    }

    @Test
    @DisplayName("注册成功 - 返回用户信息")
    void register_success() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        // When
        UserDTO result = userAccountService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("test@campus.edu", result.email());
        assertEquals("20250001", result.studentNo());
        assertEquals("test", result.nickname());
        verify(userMapper).insert(any(UserEntity.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱格式不正确")
    void register_invalidEmail() {
        // Given
        RegisterRequest request = new RegisterRequest("invalid", "20250001", "password123", "test");

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.register(request));
        assertEquals("邮箱格式不正确", exception.getMessage());
    }

    @Test
    @DisplayName("注册失败 - 学号格式不正确")
    void register_invalidStudentNo() {
        // Given
        RegisterRequest request = new RegisterRequest("test@campus.edu", "invalid", "password123", "test");

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.register(request));
        assertEquals("学号格式不正确", exception.getMessage());
    }

    @Test
    @DisplayName("注册失败 - 邮箱已注册")
    void register_emailExists() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class)))
                .thenReturn(true) // email exists
                .thenReturn(false); // studentNo not exists

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.register(registerRequest));
        assertEquals("邮箱已注册", exception.getMessage());
    }

    @Test
    @DisplayName("注册失败 - 学号已注册")
    void register_studentNoExists() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class)))
                .thenReturn(false) // email not exists
                .thenReturn(true); // studentNo exists

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.register(registerRequest));
        assertEquals("学号已注册", exception.getMessage());
    }

    @Test
    @DisplayName("登录成功 - 返回 token")
    void login_success() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.issue(eq(1L), any(Set.class))).thenReturn("test-jwt-token");
        when(jwtService.parseAndValidate("test-jwt-token")).thenReturn(
                new JwtClaims(1L, Set.of("USER"), Instant.now().plusSeconds(86400))
        );

        // When
        LoginResponse result = userAccountService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("test-jwt-token", result.token());
        assertNotNull(result.expiresAt());
    }

    @Test
    @DisplayName("登录失败 - 账号不存在")
    void login_userNotFound() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.login(loginRequest));
        assertEquals("账号或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 账号被封禁")
    void login_userBanned() {
        // Given
        UserEntity bannedUser = new UserEntity();
        bannedUser.setId(1L);
        bannedUser.setEmail("test@campus.edu");
        bannedUser.setBanned(true);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(bannedUser);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.login(loginRequest));
        assertEquals("账号已被封禁", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_wrongPassword() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.login(loginRequest));
        assertEquals("账号或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 空账号")
    void login_emptyAccount() {
        // Given
        LoginRequest request = new LoginRequest("", "password123");

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAccountService.login(request));
        assertEquals("账号或密码错误", exception.getMessage());
    }
}
