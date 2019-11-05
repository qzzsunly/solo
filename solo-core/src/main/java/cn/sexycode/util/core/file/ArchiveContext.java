package cn.sexycode.util.core.file;

/**
 * Describes the context for visiting the entries within an archive
 *
 * @author Steve Ebersole
 */
public interface ArchiveContext {
	/**
	 * Is the archive described (and being visited) the root url for the persistence-unit?
	 *
	 * @return {@code true} if it is the root url
	 */
	public boolean isRootUrl();

	/**
	 * Get the handler for the given entry, which generally is indicated by the entry type (a {@code .class} file, a
	 * mapping file, etc).
	 *
	 * @param entry The archive entry
	 *
	 * @return The appropriate handler for the entry
	 */
	public ArchiveEntryHandler obtainArchiveEntryHandler(ArchiveEntry entry);
}
