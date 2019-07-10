import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This demonstrates how a hybrid approach to workbook read can be taken, using
 * a mix of traditional XSSF and streaming one particular worksheet (perhaps one
 * which is too big for the ordinary DOM parse).
 */
public class HybridStreaming {

    private static final String SHEET_TO_STREAM = "large sheet";

    public static void main(String[] args)
            throws IOException, SAXException, OpenXML4JException, ParserConfigurationException {
        XMLReader sheetParser = SAXHelper.newXMLReader();
        try (InputStream sourceBytes = new FileInputStream("e:/x5/业务追踪表1557384063836.xlsx")) {
            XSSFWorkbook workbook = new XSSFWorkbook(sourceBytes) {
                /**
                 * Avoid DOM parse of large sheet
                 */
                @Override
                public void parseSheet(java.util.Map<String, XSSFSheet> shIdMap, CTSheet ctSheet) {
                    if (!SHEET_TO_STREAM.equals(ctSheet.getName())) {
                        super.parseSheet(shIdMap, ctSheet);
                    } else {
                        InputStream stream = ctSheet.newInputStream();
                        InputSource sheetSource = new InputSource(stream);
                        try {
                            // Having avoided a DOM-based parse of the sheet, we can stream it instead.

                            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(getPackage());
                            XSSFSheetXMLHandler xssfSheetXMLHandler = new XSSFSheetXMLHandler(getStylesSource(),
                                    strings, createSheetContentsHandler(), false);
                            sheetParser.setContentHandler(xssfSheetXMLHandler);
                            sheetParser.parse(sheetSource);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            //            workbook.close();
        }
    }

    private static SheetContentsHandler createSheetContentsHandler() {
        return new SheetContentsHandler() {

            @Override
            public void startRow(int rowNum) {
                System.out.println("111111: row: " + rowNum);
            }

            @Override
            public void endRow(int rowNum) {
            }

            @Override
            public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            }
        };
    }
}