package cn.sexycode.office.reader.excel;

import cn.sexycode.util.core.cls.internal.JavaReflectionManager;
import cn.sexycode.util.core.object.ReflectHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.sexycode.util.core.cls.XClass.ACCESS_FIELD;

/**
 * @author qinzaizhen
 */
public class SimpleExtractor<T> implements RowHandler<T> {

    private Map<String, String> mapping;

    private Class modelClass;

    public SimpleExtractor() {
        initMapping();
    }

    class Mapping {
        private String labelCol;

        private String field;

        Mapping(String labelCol, String field) {
            this.labelCol = labelCol;
            this.field = field;
        }

        String getLabelCol() {
            return labelCol;
        }

        String getField() {
            return field;
        }
    }

    private void initMapping() {
        modelClass = ReflectHelper.getParameterizedType(this.getClass());
        if (Objects.isNull(modelClass)) {
            throw new IllegalStateException("无法确定model类型");
        }
        JavaReflectionManager javaReflectionManager = new JavaReflectionManager();
        mapping = javaReflectionManager.toXClass(modelClass).getDeclaredProperties(ACCESS_FIELD).stream()
                .map(xProperty -> new Mapping(xProperty.getAnnotation(CellField.class).labelCol(), xProperty.getName()))
                .collect(Collectors.toMap(Mapping::getLabelCol, Mapping::getField));
        System.out.println(mapping);
    }

    @Override
    public T read(String labelRow, int rowNum, List<CellData> rowData) {
        Object model = null;
        try {
            model = ReflectHelper.getDefaultConstructor(modelClass).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        Object finalModel = model;
        rowData.stream().peek(data -> {
            System.out.println(data);
        }).filter(data -> mapping.containsKey(data.getLabelCol())).peek(data -> {
            String field = mapping.get(data.getLabelCol());
            try {
                ReflectHelper.findSetterMethod(modelClass, field, null).invoke(finalModel, data);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return (T) model;
    }
}
