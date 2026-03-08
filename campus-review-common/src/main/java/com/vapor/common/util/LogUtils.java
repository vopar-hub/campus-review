package com.vapor.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志工具类，提供敏感信息脱敏等功能。
 */
public class LogUtils {

    private static final Logger log = LoggerFactory.getLogger(LogUtils.class);

    // 手机号匹配
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d{9})");

    // 邮箱匹配
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    // 身份证匹配
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{17}[\\dXx]|\\d{14}[\\dXx])");

    // 密码匹配
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(password[\"']?\\s*[:=]\\s*[\"']?)([^\"',\\s]+)");

    // JWT Token 匹配
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(Bearer\\s+[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_.+/=]+)");

    /**
     * 私有构造函数，防止实例化。
     */
    private LogUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 脱敏敏感信息。
     *
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    public static String maskSensitive(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        // 脱敏手机号：13812345678 -> 138****5678
        masked = PHONE_PATTERN.matcher(masked).replaceAll(m -> {
            String phone = m.group(1);
            return phone.substring(0, 3) + "****" + phone.substring(7);
        });

        // 脱敏邮箱：test@example.com -> t**t@example.com
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(m -> {
            String email = m.group(1);
            int atIndex = email.indexOf('@');
            if (atIndex > 1) {
                return email.charAt(0) + "**" + email.substring(atIndex - 1);
            }
            return email;
        });

        // 脱敏身份证：110101199001011234 -> 110101********1234
        masked = ID_CARD_PATTERN.matcher(masked).replaceAll(m -> {
            String idCard = m.group(1);
            if (idCard.length() == 18) {
                return idCard.substring(0, 6) + "********" + idCard.substring(14);
            }
            return idCard;
        });

        // 脱敏密码
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1***");

        // 脱敏 JWT Token
        masked = TOKEN_PATTERN.matcher(masked).replaceAll("Bearer ***");

        return masked;
    }

    /**
     * 脱敏并记录 INFO 日志。
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void info(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(maskSensitive(message), maskArgs(args));
        }
    }

    /**
     * 脱敏并记录 WARN 日志。
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void warn(Logger logger, String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(maskSensitive(message), maskArgs(args));
        }
    }

    /**
     * 脱敏并记录 ERROR 日志。
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void error(Logger logger, String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(maskSensitive(message), maskArgs(args));
        }
    }

    /**
     * 脱敏并记录 DEBUG 日志。
     *
     * @param logger 日志记录器
     * @param message 日志消息
     * @param args 参数
     */
    public static void debug(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(maskSensitive(message), maskArgs(args));
        }
    }

    /**
     * 脱敏日志参数。
     *
     * @param args 原始参数
     * @return 脱敏后的参数
     */
    private static Object[] maskArgs(Object[] args) {
        if (args == null) {
            return null;
        }

        Object[] masked = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                masked[i] = maskSensitive((String) args[i]);
            } else {
                masked[i] = args[i];
            }
        }
        return masked;
    }

    /**
     * 格式化用户 ID 用于日志（部分隐藏）。
     *
     * @param userId 用户 ID
     * @return 格式化后的用户 ID
     */
    public static String formatUserId(Long userId) {
        if (userId == null) {
            return "null";
        }
        String id = String.valueOf(userId);
        if (id.length() <= 4) {
            return "***" + id;
        }
        return "****" + id.substring(id.length() - 4);
    }

    /**
     * 格式化 IP 地址用于日志（部分隐藏）。
     *
     * @param ip IP 地址
     * @return 格式化后的 IP
     */
    public static String formatIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }
        return "***";
    }
}
