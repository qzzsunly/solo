package cn.sexycode.util.core.cls;

import java.lang.annotation.Annotation;

/**
 *
 */
public interface AnnotationReader {

    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType);

    Annotation[] getAnnotations();
}