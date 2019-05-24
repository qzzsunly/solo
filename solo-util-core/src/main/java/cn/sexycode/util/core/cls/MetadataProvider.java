package cn.sexycode.util.core.cls;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

/**
 * Provides metadata
 */
public interface MetadataProvider {

    /**
     * provide default metadata
     */
    Map<Object, Object> getDefaults();

    /**
     * provide metadata for a gien annotated element
     */
    AnnotationReader getAnnotationReader(AnnotatedElement annotatedElement);
}
