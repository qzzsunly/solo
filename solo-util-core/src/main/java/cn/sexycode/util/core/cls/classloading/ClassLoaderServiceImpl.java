package cn.sexycode.util.core.cls.classloading;

import cn.sexycode.util.core.exception.ClassLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Standard implementation of the service for interacting with class loaders
 *
 * @author Steve Ebersole
 * @author Sanne Grinovero
 */
public class ClassLoaderServiceImpl implements ClassLoaderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderServiceImpl.class);

    private static final String CLASS_PATH_SCHEME = "classpath://";

    private final ConcurrentMap<Class, AggregatedServiceLoader<?>> serviceLoaders = new ConcurrentHashMap<>();

    private volatile AggregatedClassLoader aggregatedClassLoader;

    /**
     * Constructs a ClassLoaderServiceImpl with standard set-up
     */
    public ClassLoaderServiceImpl() {
        this(ClassLoaderServiceImpl.class.getClassLoader());
    }

    /**
     * Constructs a ClassLoaderServiceImpl with the given ClassLoader
     *
     * @param classLoader The ClassLoader to use
     */
    public ClassLoaderServiceImpl(ClassLoader classLoader) {
        this(Collections.singletonList(classLoader), TcclLookupPrecedence.AFTER);
    }

    /**
     * Constructs a ClassLoaderServiceImpl with the given ClassLoader instances
     *
     * @param providedClassLoaders The ClassLoader instances to use
     * @param lookupPrecedence     The lookup precedence of the thread context {@code ClassLoader}
     */
    public ClassLoaderServiceImpl(Collection<ClassLoader> providedClassLoaders, TcclLookupPrecedence lookupPrecedence) {
        final LinkedHashSet<ClassLoader> orderedClassLoaderSet = new LinkedHashSet<ClassLoader>();

        // first, add all provided class loaders, if any
        if (providedClassLoaders != null) {
            for (ClassLoader classLoader : providedClassLoaders) {
                if (classLoader != null) {
                    orderedClassLoaderSet.add(classLoader);
                }
            }
        }

        // normalize adding known class-loaders...
        // then the Hibernate class loader
        orderedClassLoaderSet.add(ClassLoaderServiceImpl.class.getClassLoader());

        // now build the aggregated class loader...
        this.aggregatedClassLoader = AccessController.doPrivileged(new PrivilegedAction<AggregatedClassLoader>() {
            @Override
            public AggregatedClassLoader run() {
                return new AggregatedClassLoader(orderedClassLoaderSet, lookupPrecedence);
            }
        });
    }

    private static void addIfSet(List<ClassLoader> providedClassLoaders, String name, Map configVales) {
        final ClassLoader providedClassLoader = (ClassLoader) configVales.get(name);
        if (providedClassLoader != null) {
            providedClassLoaders.add(providedClassLoader);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <T> Class<T> classForName(String className) {
        try {
            return (Class<T>) Class.forName(className, true, getAggregatedClassLoader());
        } catch (Exception e) {
            throw new ClassLoadingException("Unable to load class [" + className + "]", e);
        } catch (LinkageError e) {
            throw new ClassLoadingException("Unable to load class [" + className + "]", e);
        }
    }

    @Override
    public URL locateResource(String name) {
        // first we try name as a URL
        try {
            return new URL(name);
        } catch (Exception ignore) {
        }

        // if we couldn't find the resource containing a classpath:// prefix above, that means we don't have a URL
        // handler for it. So let's remove the prefix and resolve against our class loader.
        name = stripClasspathScheme(name);

        try {
            final URL url = getAggregatedClassLoader().getResource(name);
            if (url != null) {
                return url;
            }
        } catch (Exception ignore) {
        }

        if (name.startsWith("/")) {
            name = name.substring(1);

            try {
                final URL url = getAggregatedClassLoader().getResource(name);
                if (url != null) {
                    return url;
                }
            } catch (Exception ignore) {
            }
        }

        return null;
    }

    @Override
    public InputStream locateResourceStream(String name) {
        // first we try name as a URL
        try {
            LOGGER.trace("trying via [new URL(\"{}\")]", name);
            return new URL(name).openStream();
        } catch (Exception ignore) {
        }

        // if we couldn't find the resource containing a classpath:// prefix above, that means we don't have a URL
        // handler for it. So let's remove the prefix and resolve against our class loader.
        name = stripClasspathScheme(name);

        try {
            LOGGER.trace("trying via [ClassLoader.getResourceAsStream(\"{}\")]", name);
            final InputStream stream = getAggregatedClassLoader().getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        } catch (Exception ignore) {
        }

        final String stripped = name.startsWith("/") ? name.substring(1) : null;

        if (stripped != null) {
            try {
                LOGGER.trace("trying via [new URL(\"{}\")]", stripped);
                return new URL(stripped).openStream();
            } catch (Exception ignore) {
            }

            try {
                LOGGER.trace("trying via [ClassLoader.getResourceAsStream(\"{}\")]", stripped);
                final InputStream stream = getAggregatedClassLoader().getResourceAsStream(stripped);
                if (stream != null) {
                    return stream;
                }
            } catch (Exception ignore) {
            }
        }

        return null;
    }

    @Override
    public List<URL> locateResources(String name) {
        final ArrayList<URL> urls = new ArrayList<URL>();
        try {
            final Enumeration<URL> urlEnumeration = getAggregatedClassLoader().getResources(name);
            if (urlEnumeration != null && urlEnumeration.hasMoreElements()) {
                while (urlEnumeration.hasMoreElements()) {
                    urls.add(urlEnumeration.nextElement());
                }
            }
        } catch (Exception ignore) {
        }

        return urls;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> Collection<S> loadJavaServices(Class<S> serviceContract) {
        AggregatedServiceLoader<S> serviceLoader = (AggregatedServiceLoader<S>) serviceLoaders.get(serviceContract);
        if (serviceLoader == null) {
            serviceLoader = AggregatedServiceLoader.create(getAggregatedClassLoader(), serviceContract);
            serviceLoaders.put(serviceContract, serviceLoader);
        }
        return serviceLoader.getAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T generateProxy(InvocationHandler handler, Class... interfaces) {
        return (T) Proxy.newProxyInstance(getAggregatedClassLoader(), interfaces, handler);
    }

    @Override
    public <T> T workWithClassLoader(Work<T> work) {
        return work.doWork(getAggregatedClassLoader());
    }

    private AggregatedClassLoader getAggregatedClassLoader() {
        final AggregatedClassLoader aggregated = this.aggregatedClassLoader;
        if (aggregated == null) {
            //			throw log.usingStoppedClassLoaderService();
        }
        return aggregated;
    }

    private String stripClasspathScheme(String name) {
        if (name == null) {
            return null;
        }

        if (name.startsWith(CLASS_PATH_SCHEME)) {
            return name.substring(CLASS_PATH_SCHEME.length());
        }

        return name;
    }

}