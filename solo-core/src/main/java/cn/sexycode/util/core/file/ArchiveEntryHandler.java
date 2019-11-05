package cn.sexycode.util.core.file;

/**
 * Handler for archive entries, based on the classified type of the entry
 *
 * @author Steve Ebersole
 */
public interface ArchiveEntryHandler {
	/**
	 * Handle the entry
	 *
	 * @param entry The entry to handle
	 * @param context The visitation context
	 */
	void handleEntry(ArchiveEntry entry, ArchiveContext context);
}
