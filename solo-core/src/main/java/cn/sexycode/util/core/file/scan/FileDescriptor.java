package cn.sexycode.util.core.file.scan;

import java.io.InputStream;

/**
 * Descriptor for a mapping (XML) file.
 *
 * @author Steve Ebersole
 */
public interface FileDescriptor {
	/**
	 * The mapping file name.  This is its name within the archive, the
	 * expectation being that most times this will equate to a "classpath
	 * lookup resource name".
	 *
	 * @return The mapping file resource name.
	 */
	public String getName();

	/**
	 * Retrieves access to the InputStream for the mapping file.
	 *
	 * @return Access to the InputStream for the mapping file.
	 */
	public InputStream getStream();
}
