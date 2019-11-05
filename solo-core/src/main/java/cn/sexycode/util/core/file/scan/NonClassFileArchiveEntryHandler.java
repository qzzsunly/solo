package cn.sexycode.util.core.file.scan;

import cn.sexycode.util.core.file.ArchiveContext;
import cn.sexycode.util.core.file.ArchiveEntry;
import cn.sexycode.util.core.file.ArchiveEntryHandler;

/**
 * Defines handling and filtering for all non-class file (package-info is also a class file...) entries within an archive
 *
 * @author Steve Ebersole
 */
public class NonClassFileArchiveEntryHandler implements ArchiveEntryHandler {
	private final ScanResultCollector resultCollector;

	public NonClassFileArchiveEntryHandler(ScanResultCollector resultCollector) {
		this.resultCollector = resultCollector;
	}

	@Override
	public void handleEntry(ArchiveEntry entry, ArchiveContext context) {
		resultCollector.handleFile(
				new FileDescriptorImpl( entry.getNameWithinArchive(), entry.getStreamAccess() ),
				context.isRootUrl()
		);
	}
}
