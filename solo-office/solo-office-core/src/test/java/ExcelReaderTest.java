import cn.sexycode.office.reader.Model;
import cn.sexycode.office.reader.excel.CellField;
import cn.sexycode.office.reader.excel.DefaultExcelReader;
import cn.sexycode.office.reader.excel.SimpleExtractor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(JUnit4.class)
public class ExcelReaderTest {
    @Test
    public void testRead() throws IOException {
        DefaultExcelReader defaultExcelReader = new DefaultExcelReader();
        defaultExcelReader.addCellHandler((labelX, labelY, rowNum, colNum, rowData, data) -> {
            System.out.println("labelX ----> " + labelX);
            System.out.println("labelY ----> " + labelY);
            System.out.println("rowNum ----> " + rowNum);
            System.out.println("colNum ----> " + colNum);
            System.out.println("rowData ----> " + rowData);
            System.out.println("data ----> " + data);
            return null;
        });
        defaultExcelReader.read(Files.newInputStream(Paths.get("/Users/qzz/业务追踪表1557385217666.xlsx")));
    }

    @Test
    public void extractor() throws IOException {
        DefaultExcelReader defaultExcelReader = new DefaultExcelReader();
        defaultExcelReader.addRowHandler(new SimpleExtractor<>(new Model() {
            @CellField(labelX = "A")
            private String name;
        }));
        defaultExcelReader.read(Files.newInputStream(Paths.get("/Users/qzz/业务追踪表1557385217666.xlsx")));
    }
}
