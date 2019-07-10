import cn.sexycode.office.reader.excel.DefaultExcelReader;
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
        });
        defaultExcelReader.read(Files.newInputStream(Paths.get("e:/x5/业务追踪表1557384063836.xlsx")));
    }
}
