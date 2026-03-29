package com.vapor.common.util;

import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.web.UserContext;
import com.vapor.common.web.UserContextHolder;

/**
 * 用户上下文工具类。
 *
 * 提供常用的用户上下文获取和校验方法。
 */
public class UserContextUtil {

    private UserContextUtil() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID
     * @throws BizException 未登录时抛出
     */
    public static Long requireUserId() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return ctx.getUserId();
    }

    /**
     * 获取当前登录用户上下文。
     *
     * @return 用户上下文
     * @throws BizException 未登录时抛出
     */
    public static UserContext requireUserContext() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return ctx;
    }

    /**
     * 要求当前请求具备管理员角色。
     *
     * @throws BizException 非管理员时抛出
     */
    public static void requireAdmin() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || !ctx.hasRole("ADMIN")) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限");
        }
    }
    /**
     * 要求当前请求具备指定角色。
     *
     * @param role 角色名称
     * @throws BizException 不具备指定角色时抛出
     */
    public static void requireRole(String role) {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || !ctx.hasRole(role)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限");
        }
    }

    /**
     * 检查当前用户是否登录。
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isLoggedIn() {
        UserContext ctx = UserContextHolder.get();
        return ctx != null && ctx.getUserId() != null;
    }

    /**
     * 检查当前用户是否具备管理员角色。
     *
     * @return true-是管理员，false-非管理员
     */
    public static boolean isAdmin() {
        UserContext ctx = UserContextHolder.get();
        return ctx != null && ctx.hasRole("ADMIN");
    }
}
