package cn.sexycode.util.core.file;

import java.io.InputStream;

/**
 * Represent an entry in the archive.
 *
 * @author Steve Ebersole
 */
public interface ArchiveEntry {
	/**
	 * Get the entry's name
	 *
	 * @return The name
	 */
    String getName();

	/**
	 * Get the relative name of the entry within the archive.  Typically what we are looking for here is
	 * the ClassLoader resource lookup name.
	 *
	 * @return The name relative to the archive root
	 */
    String getNameWithinArchive();

	/**
	 * Get access to the stream for the entry
	 *
	 * @return Obtain stream access to the entry
	 */
    InputStream getStreamAccess();
}
