package cn.sexycode.util.core.file;

import cn.sexycode.util.core.str.StringHelper;

import java.io.File;

public class FileUtils {
    public static String toSuffix(String fullName) {
        //方式索引越界
        int docSuffixIndex = StringHelper.lastIndexOf(fullName, ".");
        if (docSuffixIndex == StringHelper.INDEX_NOT_FOUND) {
            return "";
        } else {
            return StringHelper.substring(fullName, docSuffixIndex);
        }
    }

    public static String withoutSuffix(String fullName) {
        //方式索引越界
        int docSuffixIndex = StringHelper.lastIndexOf(fullName, ".");
        if (docSuffixIndex == StringHelper.INDEX_NOT_FOUND) {
            return fullName;
        } else {
            return StringHelper.substring(fullName, 0, docSuffixIndex);
        }
    }

    public static String fileName(String fullName) {
        //方式索引越界
        int docSuffixIndex = StringHelper.lastIndexOf(new File(fullName).getAbsolutePath(), File.separator);
        if (docSuffixIndex == StringHelper.INDEX_NOT_FOUND) {
            return fullName;
        } else {
            return StringHelper.substring(fullName, docSuffixIndex + 1);
        }
    }
}
