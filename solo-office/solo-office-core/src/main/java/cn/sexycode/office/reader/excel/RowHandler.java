package cn.sexycode.office.reader.excel;

import cn.sexycode.office.reader.Model;
import cn.sexycode.util.core.lang.Order;

import java.util.List;

/**
 * 单行读取的处理器
 *
 * @author qzz
 */
public interface RowHandler extends Order {
    /**
     * @param labelX  横坐标
     * @param rowData 行数据
     * @param model   模型数据
     */
    void read(String labelX, List<Object> rowData, Model model);
}
