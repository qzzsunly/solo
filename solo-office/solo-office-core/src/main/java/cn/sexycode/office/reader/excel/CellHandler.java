package cn.sexycode.office.reader.excel;

import cn.sexycode.office.reader.Model;
import cn.sexycode.util.core.lang.Order;

import java.util.List;

/**
 * 单元格读取处理器
 *
 * @author qzz
 */
public interface CellHandler extends Order {
    /**
     * @param labelX  横坐标
     * @param labelY  竖坐标
     * @param rowData 行数据
     * @param data    原始数据
     * @param model   封装的模型数据
     */
    void read(String labelX, String labelY, List<Object> rowData, Object data, Model model);
}
