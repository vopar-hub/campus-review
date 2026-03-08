package com.vapor.common.web;

/**
 * 用户上下文线程持有器。
 *
 * 用于在一次请求的处理线程内共享 {@link UserContext}。
 */
public final class UserContextHolder {
    private static final ThreadLocal<UserContext> TL = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * 设置当前线程的用户上下文。
     *
     * @param userContext 用户上下文
     */
    public static void set(UserContext userContext) {
        TL.set(userContext);
    }

    /**
     * 获取当前线程的用户上下文。
     *
     * @return 用户上下文
     */
    public static UserContext get() {
        return TL.get();
    }

    /**
     * 清理当前线程的用户上下文。
     */
    public static void clear() {
        TL.remove();
    }
}
