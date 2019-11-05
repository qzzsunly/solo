package cn.sexycode.util.core.file;

import cn.sexycode.util.core.str.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;

/**
 * Base support for ArchiveDescriptor implementors.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractArchiveDescriptor implements ArchiveDescriptor {
    private final ArchiveDescriptorFactory archiveDescriptorFactory;

    private final URL archiveUrl;

    private final String entryBasePrefix;

    protected AbstractArchiveDescriptor(ArchiveDescriptorFactory archiveDescriptorFactory, URL archiveUrl,
            String entryBasePrefix) {
        this.archiveDescriptorFactory = archiveDescriptorFactory;
        this.archiveUrl = archiveUrl;
        this.entryBasePrefix = normalizeEntryBasePrefix(entryBasePrefix);
    }

    private static String normalizeEntryBasePrefix(String entryBasePrefix) {
        if (StringUtils.isEmpty(entryBasePrefix) || entryBasePrefix.length() == 1) {
            return null;
        }

        return entryBasePrefix.startsWith("/") ? entryBasePrefix.substring(1) : entryBasePrefix;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected ArchiveDescriptorFactory getArchiveDescriptorFactory() {
        return archiveDescriptorFactory;
    }

    protected URL getArchiveUrl() {
        return archiveUrl;
    }

    protected String getEntryBasePrefix() {
        return entryBasePrefix;
    }

    protected String extractRelativeName(ZipEntry zipEntry) {
        final String entryName = extractName(zipEntry);
        return entryBasePrefix != null && entryName.contains(entryBasePrefix) ? entryName
                .substring(entryBasePrefix.length()) : entryName;
    }

    protected String extractName(ZipEntry zipEntry) {
        return normalizePathName(zipEntry.getName());
    }

    protected String normalizePathName(String pathName) {
        return pathName.startsWith("/") ? pathName.substring(1) : pathName;
    }

    protected InputStream buildByteBasedInputStreamAccess(final String name, InputStream inputStream) {
        // because of how jar InputStreams work we need to extract the bytes immediately.  However, we
        // do delay the creation of the ByteArrayInputStreams until needed
        final byte[] bytes = ArchiveHelper.getBytesFromInputStreamSafely(inputStream);
        return new ByteArrayInputStream(bytes);
    }

}
