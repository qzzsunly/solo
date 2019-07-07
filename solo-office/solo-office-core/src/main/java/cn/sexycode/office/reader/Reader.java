package cn.sexycode.office.reader;

import java.io.InputStream;

/**
 * 阅读器
 *
 * @author qzz
 */
public interface Reader {
    void read(InputStream in, Model model) throws ParseException;
}
