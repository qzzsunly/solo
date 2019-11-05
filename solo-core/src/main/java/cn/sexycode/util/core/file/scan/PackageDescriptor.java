package cn.sexycode.util.core.file.scan;


import java.io.InputStream;

/**
 * Descriptor for a package (as indicated by a package-info.class file).
 *
 * @author Steve Ebersole
 */
public interface PackageDescriptor {
	/**
	 * Retrieves the package name.
	 *
	 * @return The package name
	 */
	public String getName();

	/**
	 * Retrieves access to the InputStream for the {@code package-info.class} file.
	 *
	 * @return Access to the InputStream for the {@code package-info.class} file.
	 */
	public InputStream getStream();
}
