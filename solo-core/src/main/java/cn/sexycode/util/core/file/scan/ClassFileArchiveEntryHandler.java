package cn.sexycode.util.core.file.scan;

import cn.sexycode.util.core.file.ArchiveContext;
import cn.sexycode.util.core.file.ArchiveEntry;
import cn.sexycode.util.core.file.ArchiveEntryHandler;

/**
 * Defines handling and filtering for class file entries within an archive
 *
 * @author Steve Ebersole
 */
public class ClassFileArchiveEntryHandler implements ArchiveEntryHandler {
	protected final ScanResultCollector resultCollector;

	public ClassFileArchiveEntryHandler(ScanResultCollector resultCollector) {
		this.resultCollector = resultCollector;
	}

	@Override
	public void handleEntry(ArchiveEntry entry, ArchiveContext context) {
		// Ultimately we'd like to leverage Jandex here as long term we want to move to
		// using Jandex for annotation processing.  But even then, Jandex atm does not have
		// any facility for passing a stream and conditionally indexing it into an Index or
		// returning existing ClassInfo objects.
		//
		// So not sure we can ever not do this unconditional input stream read :(
//		final ClassFile classFile = toClassFile( entry );
//		final ClassDescriptor classDescriptor = toClassDescriptor( classFile, entry );

//		if ( classDescriptor.getCategorization() == ClassDescriptor.Categorization.OTHER ) {
//			return;
//		}

		resultCollector.handleClass( null, context.isRootUrl() );
	}


}
