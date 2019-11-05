package cn.sexycode.util.core.i18n;

import cn.sexycode.util.core.exception.ExceptionMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author qinzaizhen
 */
public class I18NUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(I18NUtil.class);

    public static String getMessage(String bundleKey, String code) {
        return getMessage(bundleKey, code, null, getLocal());
    }

    public static Locale getLocal() {
        return Locale.getDefault();
    }

    public static String getMessage(String bundleKey, String code, Locale locale) {
        return getMessage(bundleKey, code, null, locale);
    }

    public static String getMessage(String bundleKey, String code, Object[] args) {
        return getMessage(bundleKey, code, args, getLocal());
    }

    public static String getMessage(String bundleKey, String code, Object[] args, String defaultMessage,
            Locale locale) {
        String result = getMessage(bundleKey, code, args, locale);
        if (result == null) {
            return defaultMessage;
        }
        return result;
    }

    public static String getMessage(String bundleKey, String code, Object[] args, Locale locale) {
        try {

            ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleKey, locale);
            String bundleString = resourceBundle.getString(code);
            return MessageFormat.format(bundleString, args);
        } catch (Exception e) {
            LOGGER.error(ExceptionMessageUtil.getMessage("message.query.fail", new Object[]{code}), e);
            return null;
        }
    }

    /**
     * 根据CODE查询，自定义默认值，默认无通配参数
     */
    public static String getMessage(String bundleKey, String code, String defaultMessage) {
        return getMessage(bundleKey, code, null, defaultMessage, getLocal());
    }


}
