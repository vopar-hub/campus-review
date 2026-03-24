package com.vapor.user.service;

import com.vapor.common.error.BizException;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import com.vapor.model.user.UserDTO;
import com.vapor.user.entity.UserEntity;
import com.vapor.user.mapper.UserMapper;
import com.vapor.user.service.impl.UserAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserAdminServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserAdminServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    @BeforeEach
    void setUp() {
        userAdminService = new UserAdminServiceImpl(userMapper);
    }

    @Test
    @DisplayName("封禁用户成功")
    void banUser_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            // 使用 any() 匹配所有参数，避免 lambda cache 问题
            when(userMapper.update(any(), any())).thenReturn(1);

            // When
            assertDoesNotThrow(() -> userAdminService.ban(1L));

            // Then
            verify(userMapper).update(any(), any());
        }
    }

    @Test
    @DisplayName("封禁用户 - 用户不存在")
    void banUser_notFound() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(userMapper.update(any())).thenReturn(0);

            // When & Then
            BizException exception = assertThrows(BizException.class, () -> userAdminService.ban(1L));
            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Test
    @DisplayName("解封用户成功")
    void unbanUser_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(userMapper.update(any())).thenReturn(1);

            // When
            assertDoesNotThrow(() -> userAdminService.unban(1L));

            // Then
            verify(userMapper).update(any());
        }
    }

    @Test
    @DisplayName("解封用户 - 用户不存在")
    void unbanUser_notFound() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(userMapper.update(any())).thenReturn(0);

            // When & Then
            BizException exception = assertThrows(BizException.class, () -> userAdminService.unban(1L));
            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Test
    @DisplayName("获取用户列表成功")
    void getUserList_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            UserEntity entity1 = new UserEntity();
            entity1.setId(1L);
            entity1.setEmail("zhangsan@example.edu.cn");
            entity1.setStudentNo("2024001");
            entity1.setNickname("张三");
            entity1.setRoles("USER");
            entity1.setBanned(false);
            entity1.setCreatedAt(Instant.now());

            UserEntity entity2 = new UserEntity();
            entity2.setId(2L);
            entity2.setEmail("lisi@example.edu.cn");
            entity2.setStudentNo("2024002");
            entity2.setNickname("李四");
            entity2.setRoles("ADMIN");
            entity2.setBanned(false);
            entity2.setCreatedAt(Instant.now());

            when(userMapper.selectList(any())).thenReturn(List.of(entity1, entity2));

            // When
            List<UserDTO> result = userAdminService.getUserList();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("张三", result.get(0).nickname());
            assertEquals("USER", result.get(0).roles().iterator().next());
            assertEquals("李四", result.get(1).nickname());
            assertEquals("ADMIN", result.get(1).roles().iterator().next());
        }
    }

    @Test
    @DisplayName("获取用户列表 - 空列表")
    void getUserList_empty() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            when(userMapper.selectList(any())).thenReturn(List.of());

            // When
            List<UserDTO> result = userAdminService.getUserList();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("获取用户列表 - 用户角色为空")
    void getUserList_nullRoles() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            UserEntity entity = new UserEntity();
            entity.setId(1L);
            entity.setEmail("test@example.edu.cn");
            entity.setStudentNo("2024003");
            entity.setNickname("测试用户");
            entity.setRoles(null);
            entity.setBanned(false);
            entity.setCreatedAt(Instant.now());

            when(userMapper.selectList(any())).thenReturn(List.of(entity));

            // When
            List<UserDTO> result = userAdminService.getUserList();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).roles().isEmpty());
        }
    }

    @Test
    @DisplayName("获取用户列表 - 用户已封禁")
    void getUserList_bannedUser() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            UserEntity entity = new UserEntity();
            entity.setId(1L);
            entity.setEmail("banned@example.edu.cn");
            entity.setStudentNo("2024004");
            entity.setNickname("被封禁用户");
            entity.setRoles("USER");
            entity.setBanned(true);
            entity.setCreatedAt(Instant.now());

            when(userMapper.selectList(any())).thenReturn(List.of(entity));

            // When
            List<UserDTO> result = userAdminService.getUserList();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).banned());
        }
    }

    @Test
    @DisplayName("获取用户列表 - 多角色用户")
    void getUserList_multipleRoles() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserContext).thenReturn(userContext);
            mocked.when(UserContextUtil::requireUserId).thenReturn(100L);
            mocked.when(UserContextUtil::requireAdmin).thenAnswer(invocation -> null);

            UserEntity entity = new UserEntity();
            entity.setId(1L);
            entity.setEmail("admin@example.edu.cn");
            entity.setStudentNo("2024005");
            entity.setNickname("超级管理员");
            entity.setRoles("USER,ADMIN,SUPER_ADMIN");
            entity.setBanned(false);
            entity.setCreatedAt(Instant.now());

            when(userMapper.selectList(any())).thenReturn(List.of(entity));

            // When
            List<UserDTO> result = userAdminService.getUserList();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Set<String> roles = result.get(0).roles();
            assertEquals(3, roles.size());
            assertTrue(roles.contains("USER"));
            assertTrue(roles.contains("ADMIN"));
            assertTrue(roles.contains("SUPER_ADMIN"));
        }
    }
}
