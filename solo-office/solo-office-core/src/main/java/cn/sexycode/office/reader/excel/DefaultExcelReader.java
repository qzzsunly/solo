package cn.sexycode.office.reader.excel;

import cn.sexycode.office.Config;
import cn.sexycode.office.reader.ParseException;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author qzz
 */
public class DefaultExcelReader implements ExcelReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExcelReader.class);

    private List<CellHandler> cellHandlers = new ArrayList<>();

    private List<RowHandler> rowHandlers = new ArrayList<>();

    private List<SheetHandler> sheetHandlers = new ArrayList<>();

    @Override
    public void setCellHandlers(List<CellHandler> cellHandlers) {
        this.cellHandlers.addAll(Optional.of(cellHandlers).orElse(new ArrayList<>()));
    }

    @Override
    public List<CellHandler> getCellHandlers() {
        return this.cellHandlers;
    }

    @Override
    public void setRowHandlers(List<RowHandler> rowHandlers) {
        this.rowHandlers.addAll(Optional.of(rowHandlers).orElse(new ArrayList<>()));
    }

    @Override
    public List<RowHandler> getRowHandlers() {
        return this.rowHandlers;
    }

    @Override
    public void setSheetHandlers(List<SheetHandler> sheetHandlers) {
        this.sheetHandlers.addAll(Optional.of(sheetHandlers).orElse(new ArrayList<>()));
    }

    @Override
    public List<SheetHandler> getSheetHandlers() {
        return this.sheetHandlers;
    }

    @Override
    public void read(InputStream in) throws ParseException {
        try {
            OPCPackage pkg = OPCPackage.open(in);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();

            XMLReader parser = SAXHelper.newXMLReader();
            XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(r.getStylesTable(), sst, new InnerSheetHandler(),
                    false);
            parser.setContentHandler(handler);
            Iterator<InputStream> sheets = r.getSheetsData();
            while (sheets.hasNext()) {
                System.out.println("Processing new sheet:\n");
                InputStream sheet = sheets.next();
                InputSource sheetSource = new InputSource(sheet);
                parser.parse(sheetSource);
                sheet.close();
                System.out.println("");
            }
        } catch (Exception e) {

            LOGGER.error("异常", e);
            throw new ParseException(e.getMessage());
        }
    }

    private class InnerSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private List<Object> rowData;

        private int currentRow = -1;

        private int currentCol = -1;

        @Override
        public void startRow(int rowNum) {
            currentRow = rowNum;
            currentCol = -1;
            rowData = new ArrayList<>();
            //            System.out.println("startRow: " + rowNum);
        }

        @Override
        public void endRow(int rowNum) {
            //            System.out.println("endRow: " + rowNum);
            if (!Config.cellHandlerSkipRowData) {
                getRowHandlers().forEach(rowHandler -> rowHandler.read(String.valueOf(rowNum + 1), rowNum, rowData));
                for (int i = 0; i < rowData.size(); i++) {
                    Object data = rowData.get(i);
                    String labelX = String.valueOf(currentRow + 1);
                    String labelY = CellReference.convertNumToColString(i);
                    int col = i;
                    getCellHandlers()
                            .forEach(cellHandler -> cellHandler.read(labelX, labelY, currentRow, col, rowData, data));
                }

            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if (cellReference == null) {
                cellReference = new CellAddress(currentRow, currentCol).formatAsString();
            }

            // Did we miss any cells?
            currentCol = (new CellReference(cellReference)).getCol();
            String labelX = CellReference.convertNumToColString(currentCol);
            rowData.add(formattedValue);
            if (Config.cellHandlerSkipRowData) {
                getCellHandlers().forEach(cellHandler -> {
                    String labelY = String.valueOf(currentCol + 1);
                    cellHandler.read(labelX, labelY, currentRow, currentCol, rowData, formattedValue);
                });
            }
            //            System.out.println(formattedValue);
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {

        }

        @Override
        public void endSheet() {

        }
    }
}