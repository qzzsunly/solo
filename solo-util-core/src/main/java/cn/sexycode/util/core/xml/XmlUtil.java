package cn.sexycode.util.core.xml;

import cn.sexycode.util.core.str.StringHelper;
import cn.sexycode.util.core.str.StringPool;

public class XmlUtil {
    public static String decode(String source) {
        return StringHelper.replace(source, StringPool.HTML_LT, StringPool.LEFT_CHEV)
                .replace(StringPool.HTML_GT, StringPool.RIGHT_CHEV);
    }
}
