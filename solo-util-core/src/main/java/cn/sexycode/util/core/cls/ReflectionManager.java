package cn.sexycode.util.core.cls;

import cn.sexycode.util.core.exception.ClassLoadingException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The entry point to the reflection layer (a.k.a. the X* layer).
 */
public interface ReflectionManager {
    /**
     * Allows injection of a ClassLoaderDelegate into the ReflectionManager
     *
     * @param delegate The ClassLoaderDelegate to use
     */
    void injectClassLoaderDelegate(ClassLoaderDelegate delegate);

    /**
     * Access to the ClassLoaderDelegate currently associated with this ReflectionManager
     *
     * @return The current ClassLoaderDelegate
     */
    ClassLoaderDelegate getClassLoaderDelegate();

    <T> XClass toXClass(Class<T> clazz);

    Class toClass(XClass xClazz);

    Method toMethod(XMethod method);

    /**
     * Given the name of a Class, retrieve the XClass representation.
     * <p/>
     * Uses ClassLoaderDelegate (via {@link #getClassLoaderDelegate()}) to resolve the Class reference
     *
     * @param name The name of the Class to load (as an XClass)
     * @return The XClass instance
     * @throws ClassLoadingException Indicates a problem resolving the Class; see {@link ClassLoaderDelegate#classForName}
     */
    XClass classForName(String name) throws ClassLoadingException;

    XPackage packageForName(String packageName) throws ClassNotFoundException;

    <T> boolean equals(XClass class1, Class<T> class2);

    AnnotationReader buildAnnotationReader(AnnotatedElement annotatedElement);

    Map getDefaults();
}
