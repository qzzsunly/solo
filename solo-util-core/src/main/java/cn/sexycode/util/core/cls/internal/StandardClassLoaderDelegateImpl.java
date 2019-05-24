package cn.sexycode.util.core.cls.internal;

import cn.sexycode.util.core.cls.ClassLoaderDelegate;
import cn.sexycode.util.core.exception.ClassLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class StandardClassLoaderDelegateImpl implements ClassLoaderDelegate {
    /**
     * Singleton access
     */
    public static final StandardClassLoaderDelegateImpl INSTANCE = new StandardClassLoaderDelegateImpl();

    private static final Logger log = LoggerFactory.getLogger(StandardClassLoaderDelegateImpl.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> classForName(String className) throws ClassLoadingException {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                return (Class<T>) Class.forName(className, true, contextClassLoader);
            }
        } catch (Throwable ignore) {
            log.debug("Unable to locate Class [%s] using TCCL, falling back to HCANN ClassLoader", className);
        }

        try {
            return (Class<T>) Class.forName(className, true, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClassLoadingException("Unable to load Class [" + className + "]", e);
        }
    }
}
