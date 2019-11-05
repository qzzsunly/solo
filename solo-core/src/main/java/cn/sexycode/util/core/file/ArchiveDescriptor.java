package cn.sexycode.util.core.file;

/**
 * Contract for visiting an archive, which might be a jar, a zip, an exploded directory, etc.
 *
 * @author Steve Ebersole
 * @author Emmanuel Bernard
 */
public interface ArchiveDescriptor {
	/**
	 * Perform visitation using the given context
	 *
	 * @param archiveContext The visitation context
	 */
	void visitArchive(ArchiveContext archiveContext);
}
