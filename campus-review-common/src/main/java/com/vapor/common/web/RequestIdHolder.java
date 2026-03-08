package com.vapor.common.web;

/**
 * 请求 ID 线程上下文持有器。
 *
 * 用于在一次请求的处理线程内共享 requestId（例如在异常处理时取出写入响应体）。
 */
public final class RequestIdHolder {
    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    private RequestIdHolder() {
    }

    /**
     * 设置当前线程的 requestId。
     *
     * @param requestId 请求 ID
     */
    public static void set(String requestId) {
        TL.set(requestId);
    }

    /**
     * 获取当前线程的 requestId。
     *
     * @return requestId
     */
    public static String get() {
        return TL.get();
    }

    /**
     * 清理当前线程的 requestId。
     */
    public static void clear() {
        TL.remove();
    }
}
