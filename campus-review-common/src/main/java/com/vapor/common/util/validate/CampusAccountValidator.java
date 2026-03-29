package com.vapor.common.util.validate;

import java.util.regex.Pattern;

/**
 * 校园账号格式校验工具。
 *
 * 用于校验邮箱与学号等账号字段的基本格式合法性。
 */
public final class CampusAccountValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern STUDENT_NO_PATTERN = Pattern.compile("^[0-9]{6,20}$");

    private CampusAccountValidator() {
    }

    /**
     * 校验邮箱格式是否合法。
     *
     * @param email 邮箱
     * @return 是否合法
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 校验学号格式是否合法。
     *
     * @param studentNo 学号
     * @return 是否合法
     */
    public static boolean isValidStudentNo(String studentNo) {
        return studentNo != null && STUDENT_NO_PATTERN.matcher(studentNo).matches();
    }
}
