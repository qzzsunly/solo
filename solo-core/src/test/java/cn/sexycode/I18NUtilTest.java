package cn.sexycode;

import cn.sexycode.util.core.exception.ExceptionMessageUtil;
import org.junit.Test;

public class I18NUtilTest {
    @Test
    public void getMessageTest(){
        System.out.println(ExceptionMessageUtil.getMessage("message.query.fail", new Object[]{"query"}));
    }
}
