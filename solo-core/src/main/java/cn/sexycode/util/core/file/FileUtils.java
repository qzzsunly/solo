package cn.sexycode.util.core.file;

import cn.sexycode.util.core.str.StringUtils;

import java.io.File;

public class FileUtils {
    public static String toSuffix(String fullName) {
        //方式索引越界
        int docSuffixIndex = StringUtils.lastIndexOf(fullName, ".");
        if (docSuffixIndex == StringUtils.INDEX_NOT_FOUND) {
            return "";
        } else {
            return StringUtils.substring(fullName, docSuffixIndex);
        }
    }

    public static String withoutSuffix(String fullName) {
        //方式索引越界
        int docSuffixIndex = StringUtils.lastIndexOf(fullName, ".");
        if (docSuffixIndex == StringUtils.INDEX_NOT_FOUND) {
            return fullName;
        } else {
            return StringUtils.substring(fullName, 0, docSuffixIndex);
        }
    }

    public static String fileName(String fullName) {
        //方式索引越界
        int docSuffixIndex = StringUtils.lastIndexOf(new File(fullName).getAbsolutePath(), File.separator);
        if (docSuffixIndex == StringUtils.INDEX_NOT_FOUND) {
            return fullName;
        } else {
            return StringUtils.substring(fullName, docSuffixIndex + 1);
        }
    }
}
