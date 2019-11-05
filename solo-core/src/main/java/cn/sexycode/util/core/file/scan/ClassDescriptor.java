package cn.sexycode.util.core.file.scan;

import java.io.InputStream;

/**
 * Descriptor for a class file.
 *
 * @author Steve Ebersole
 */
public interface ClassDescriptor {
	interface Categorization{}
	enum CategorizationEnum implements Categorization{
		MODEL,
		CONVERTER,
		OTHER
	}

	/**
	 * Retrieves the class name, not the file name.
	 *
	 * @return The name (FQN) of the class
	 */
	String getName();

	Categorization getCategorization();

	/**
	 * Retrieves access to the InputStream for the class file.
	 *
	 * @return Access to the InputStream for the class file.
	 */
	InputStream getStream();
}
