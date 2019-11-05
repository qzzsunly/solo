package cn.sexycode.util.core.xml;

import cn.sexycode.util.core.str.StringPool;
import cn.sexycode.util.core.str.StringUtils;

public class XmlUtil {
    public static String decode(String source) {
        return StringUtils.replace(source, StringPool.HTML_LT, StringPool.LEFT_CHEV)
                .replace(StringPool.HTML_GT, StringPool.RIGHT_CHEV);
    }
}
