package cn.sexycode.office.reader.excel;

import cn.sexycode.office.reader.Model;
import cn.sexycode.office.reader.ParseException;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
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
    public void read(InputStream in, Model model) throws ParseException {
        try {
            OPCPackage pkg = OPCPackage.open(in);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();

            XMLReader parser = fetchSheetParser(sst);

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

    private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException, ParserConfigurationException {
        XMLReader parser = SAXHelper.newXMLReader();
        ContentHandler handler = new InnerSheetHandler(sst);
        parser.setContentHandler(handler);
        return parser;
    }

    /**
     * See org.xml.sax.helpers.DefaultHandler javadocs
     */
    private class InnerSheetHandler extends DefaultHandler {
        private SharedStringsTable sst;

        private String lastContents;

        private boolean nextIsString;

        private InnerSheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            // c => cell
            if (name.equals("c")) {
                // Print the cell reference
                System.out.print(attributes.getValue("r") + " - ");
                // Figure out if the value is an index in the SST
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = sst.getItemAt(idx).getString();
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if (name.equals("v")) {
                System.out.println(lastContents);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }
    }
}