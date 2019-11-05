package cn.sexycode.util.core.cls.internal;

import cn.sexycode.util.core.cls.AnnotationReader;
import cn.sexycode.util.core.cls.MetadataProvider;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class JavaMetadataProvider implements MetadataProvider {

    public Map<Object, Object> getDefaults() {
        return Collections.emptyMap();
    }

    public AnnotationReader getAnnotationReader(AnnotatedElement annotatedElement) {
        return new JavaAnnotationReader(annotatedElement);
    }
}
