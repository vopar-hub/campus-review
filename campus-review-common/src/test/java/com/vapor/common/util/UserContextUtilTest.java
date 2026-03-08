package com.vapor.common.util;

import com.vapor.common.error.BizException;
import com.vapor.common.web.UserContext;
import com.vapor.common.web.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserContextUtil 单元测试。
 */
class UserContextUtilTest {

    @BeforeEach
    void setUp() {
        // 清理线程变量
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void testRequireUserId_WhenLoggedIn() {
        UserContext context = new UserContext(123L, Set.of("USER"));
        UserContextHolder.set(context);

        Long userId = UserContextUtil.requireUserId();
        assertEquals(123L, userId);
    }

    @Test
    void testRequireUserId_WhenNotLoggedIn() {
        assertThrows(BizException.class, UserContextUtil::requireUserId);
    }

    @Test
    void testRequireUserContext_WhenLoggedIn() {
        UserContext context = new UserContext(456L, Set.of("USER", "ADMIN"));
        UserContextHolder.set(context);

        UserContext result = UserContextUtil.requireUserContext();
        assertNotNull(result);
        assertEquals(456L, result.getUserId());
    }

    @Test
    void testRequireUserContext_WhenNotLoggedIn() {
        assertThrows(BizException.class, UserContextUtil::requireUserContext);
    }

    @Test
    void testRequireAdmin_WhenUserIsAdmin() {
        UserContext context = new UserContext(789L, Set.of("ADMIN"));
        UserContextHolder.set(context);

        // 不应该抛出异常
        assertDoesNotThrow(UserContextUtil::requireAdmin);
    }

    @Test
    void testRequireAdmin_WhenUserIsNotAdmin() {
        UserContext context = new UserContext(111L, Set.of("USER"));
        UserContextHolder.set(context);

        assertThrows(BizException.class, UserContextUtil::requireAdmin);
    }

    @Test
    void testRequireRole_WhenUserHasRole() {
        UserContext context = new UserContext(222L, Set.of("USER", "EDITOR"));
        UserContextHolder.set(context);

        assertDoesNotThrow(() -> UserContextUtil.requireRole("EDITOR"));
    }

    @Test
    void testRequireRole_WhenUserDoesNotHaveRole() {
        UserContext context = new UserContext(333L, Set.of("USER"));
        UserContextHolder.set(context);

        assertThrows(BizException.class, () -> UserContextUtil.requireRole("ADMIN"));
    }

    @Test
    void testIsLoggedIn_WhenLoggedIn() {
        UserContext context = new UserContext(444L, Set.of("USER"));
        UserContextHolder.set(context);

        assertTrue(UserContextUtil.isLoggedIn());
    }

    @Test
    void testIsLoggedIn_WhenNotLoggedIn() {
        assertFalse(UserContextUtil.isLoggedIn());
    }

    @Test
    void testIsAdmin_WhenUserIsAdmin() {
        UserContext context = new UserContext(555L, Set.of("ADMIN"));
        UserContextHolder.set(context);

        assertTrue(UserContextUtil.isAdmin());
    }

    @Test
    void testIsAdmin_WhenUserIsNotAdmin() {
        UserContext context = new UserContext(666L, Set.of("USER"));
        UserContextHolder.set(context);

        assertFalse(UserContextUtil.isAdmin());
    }

    @Test
    void testIsAdmin_WhenNotLoggedIn() {
        assertFalse(UserContextUtil.isAdmin());
    }
}
