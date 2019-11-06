package cn.sexycode.util.core.cls.classloading;

import java.net.URL;

/**
 * During the process of building this metamodel, accessing the ClassLoader
 * is very discouraged.  However, sometimes it is needed.  This contract helps
 * mitigate accessing the ClassLoader in these cases.
 *
 * @author Steve Ebersole
 *
 * @since 5.0
 */
public interface ClassLoaderAccess {
	/**
	 * Obtain a Class reference by name
	 *
	 * @param name The name of the Class to get a reference to.
	 *
	 * @return The Class.
	 */
	<T> Class<T> classForName(String name);

	/**
	 * Locate a resource by name
	 *
	 * @param resourceName The name of the resource to resolve.
	 *
	 * @return The located resource; may return {@code null} to indicate the resource was not found
	 */
	URL locateResource(String resourceName);
}
