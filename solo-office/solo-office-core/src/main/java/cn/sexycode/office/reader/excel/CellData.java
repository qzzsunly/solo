package cn.sexycode.office.reader.excel;

import org.apache.poi.ss.util.CellReference;

public class CellData {
    private String labelY;

    private String labelX;

    private Object data;

    private int rowNum;

    private int colNum;

    public CellData(String labelX, String labelY, Object data) {
        this.labelY = labelY;
        this.labelX = labelX;
        this.rowNum = Integer.valueOf(labelY) - 1;
        this.colNum = CellReference.convertColStringToIndex(labelY);
        this.data = data;
    }

    public String getLabelY() {
        return labelY;
    }

    public String getLabelX() {
        return labelX;
    }

    public Object getData() {
        return data;
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }
}
