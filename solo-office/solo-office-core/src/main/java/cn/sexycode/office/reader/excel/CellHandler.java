package cn.sexycode.office.reader.excel;

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
     * @param rowNum
     * @param colNum
     * @param rowData 行数据 不要修改list的长度，内部有遍历
     * @param data    原始数据
     */
    void read(String labelX, String labelY, int rowNum, int colNum, List<Object> rowData, Object data);
}
