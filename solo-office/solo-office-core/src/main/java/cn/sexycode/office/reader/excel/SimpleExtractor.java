package cn.sexycode.office.reader.excel;

import cn.sexycode.util.core.cls.internal.JavaReflectionManager;
import cn.sexycode.util.core.object.ReflectHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.sexycode.util.core.cls.XClass.ACCESS_FIELD;

public class SimpleExtractor<T> implements RowHandler<T> {
    private T model;

    private Map<String, String> mapping;

    public SimpleExtractor(T model) {
        this.model = model;
        initMapping();
    }

    class Mapping {
        private String labelY;

        private String field;

        public Mapping(String labelY, String field) {
            this.labelY = labelY;
            this.field = field;
        }

        public String getLabelY() {
            return labelY;
        }

        public String getField() {
            return field;
        }
    }

    private void initMapping() {
        JavaReflectionManager javaReflectionManager = new JavaReflectionManager();
        mapping = javaReflectionManager.toXClass(model.getClass()).getDeclaredProperties(ACCESS_FIELD).stream()
                .map(xProperty -> new Mapping(xProperty.getAnnotation(CellField.class).labelX(), xProperty.getName()))
                .collect(Collectors.toMap(Mapping::getLabelY, Mapping::getField));
        System.out.println(mapping);
    }

    @Override
    public T read(String labelX, int rowNum, List<CellData> rowData) {
        rowData.stream().filter(data -> mapping.containsKey(data.getLabelY())).map(data -> {
            String field = mapping.get(data.getLabelY());
            ReflectHelper.findSetterMethod(model.getClass(), field, )
        }); return model;
    }
}
