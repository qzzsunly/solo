package cn.sexycode.util.core.cls.classloading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Standard implementation of ClassLoaderAccess
 *
 * @author Steve Ebersole
 */
public class ClassLoaderAccessImpl implements ClassLoaderAccess {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderAccessImpl.class);
    private final ClassLoaderService classLoaderService;

    public ClassLoaderAccessImpl(ClassLoaderService classLoaderService) {
        this.classLoaderService = classLoaderService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<?> classForName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of class to load cannot be null");
        }

        if (isSafeClass(name)) {
            return classLoaderService.classForName(name);
        } else {
			LOGGER.debug( "Not known whether passed class name [{}] is safe", name );
//			if ( jpaTempClassLoader == null ) {
				/*log.debugf(
						"No temp ClassLoader provided; using live ClassLoader " +
								"for loading potentially unsafe class : %s",
						name
				);*/
				return classLoaderService.classForName( name );
			/*}
			else {
				log.debugf(
						"Temp ClassLoader was provided, so we will use that : %s",
						name
				);
				try {
					return jpaTempClassLoader.loadClass( name );
				}
				catch (ClassNotFoundException e) {
					throw new ClassLoadingException( name );
				}
			}*/
        }
//        throw new ClassLoadingException(name);
    }

    private boolean isSafeClass(String name) {
        // classes in any of these packages are safe to load through the "live" ClassLoader
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("org.hibernate.");

    }

    @Override
    public URL locateResource(String resourceName) {
        return classLoaderService.locateResource(resourceName);
    }

    public void release() {
    }
}
