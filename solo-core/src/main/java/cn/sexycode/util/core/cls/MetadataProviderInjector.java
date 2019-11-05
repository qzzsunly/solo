package cn.sexycode.util.core.cls;

/**
 * Offers access to and the ability to change the metadata provider
 */
public interface MetadataProviderInjector {
    MetadataProvider getMetadataProvider();

    /**
     * Defines the metadata provider for a given Reflection Manager
     */
    void setMetadataProvider(MetadataProvider metadataProvider);
}
