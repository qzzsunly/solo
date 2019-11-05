package cn.sexycode.util.core.file.scan;

/**
 * Options for performing scanning
 *
 * @author Steve Ebersole
 */
public interface ScanOptions {
	/**
	 * Is detection of managed classes from root url allowed?  In strict JPA
	 * sense, this would be controlled by the {@code <exclude-unlisted-classes/>}
	 * element.
	 *
	 * @return Whether detection of classes from root url is allowed
	 */
	public boolean canDetectUnlistedClassesInRoot();

	/**
	 * Is detection of managed classes from non-root urls allowed?  In strict JPA
	 * sense, this would always be allowed.
	 *
	 * @return Whether detection of classes from non-root urls is allowed
	 */
	public boolean canDetectUnlistedClassesInNonRoot();

}
