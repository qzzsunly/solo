package cn.sexycode.office.reader.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在字段上映射单元格
 *
 * @author qzz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CellField {
    /**
     * 横坐标
     *
     * @return 横坐标
     */
    String labelX();
}
