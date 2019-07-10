package cn.sexycode.office.reader;

import java.io.InputStream;

/**
 * 阅读器
 *
 * @author qzz
 */
public interface Reader {
    /**
     * @param in
     * @throws ParseException
     */
    void read(InputStream in) throws ParseException;
}
