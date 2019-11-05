package cn.sexycode.util.core.exception;

import cn.sexycode.util.core.i18n.I18NUtil;

/**
 * @author qinzaizhen
 * 获取国际化异常信息
 */
public class ExceptionMessageUtil {
    private static final String MESSAGE_KEY = "exception";

    public static String getMessage(String code, Object[] args) {
        return I18NUtil.getMessage(MESSAGE_KEY, code, args);
    }

    public static String getMessage(String code) {
        return getMessage(code, null);
    }
}
