package cn.sexycode.util.core.file;

import java.net.URL;

/**
 * Optional contract for ArchiveDescriptorFactory implementations to implement
 * to be able to adjust {@code <jar-file/>} URL's defined in {@code persistence.xml}
 * files.  The intent is to account for relative URLs in a protocol specific way
 * according to the protocol(s) handled by the ArchiveDescriptorFactory.
 *
 * @author Steve Ebersole
 */
public interface JarFileEntryUrlAdjuster {
    /**
     * Adjust the given URL, if needed.
     *
     * @param url     The url to adjust
     * @param rootUrl The root URL, for resolving relative URLs
     * @return The adjusted url.
     */
    URL adjustJarFileEntryUrl(URL url, URL rootUrl);
}
