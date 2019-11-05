package cn.sexycode.util.core.file.scan;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ScanResultCollectorImpl implements ScanResultCollector {
    private final ScanEnvironment environment;

    private final ScanOptions options;

    private final Set<ClassDescriptor> discoveredClasses;

    private final Set<PackageDescriptor> discoveredPackages;

    private final Set<FileDescriptor> discoveredMappingFiles;

    public ScanResultCollectorImpl(ScanEnvironment environment, ScanOptions options, ScanParameters parameters) {

        this.environment = environment;
        this.options = options;

        if (environment.getExplicitlyListedClassNames() == null) {
            throw new IllegalArgumentException("ScanEnvironment#getExplicitlyListedClassNames should not return null");
        }

        this.discoveredPackages = new HashSet<PackageDescriptor>();
        this.discoveredClasses = new HashSet<ClassDescriptor>();
        this.discoveredMappingFiles = new HashSet<FileDescriptor>();
    }

    public void handleClass(ClassDescriptor classDescriptor, boolean rootUrl) {
        if (!isListedOrDetectable(classDescriptor.getName(), rootUrl)) {
            return;
        }

        discoveredClasses.add(classDescriptor);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    protected boolean isListedOrDetectable(String name, boolean rootUrl) {
        // IMPL NOTE : protect the calls to getExplicitlyListedClassNames unless needed,
        // since it can take time with lots of listed classes.
        if (rootUrl) {
            // The entry comes from the root url.  Allow it if either:
            //		1) we are allowed to discover classes/packages in the root url
            //		2) the entry was explicitly listed
            return options.canDetectUnlistedClassesInRoot() || environment.getExplicitlyListedClassNames()
                    .contains(name);
        } else {
            // The entry comes from a non-root url.  Allow it if either:
            //		1) we are allowed to discover classes/packages in non-root urls
            //		2) the entry was explicitly listed
            return options.canDetectUnlistedClassesInNonRoot() || environment.getExplicitlyListedClassNames()
                    .contains(name);
        }
    }

    public void handlePackage(PackageDescriptor packageDescriptor, boolean rootUrl) {
        if (!isListedOrDetectable(packageDescriptor.getName(), rootUrl)) {
            // not strictly needed, but helps cut down on the size of discoveredPackages
            return;
        }

        discoveredPackages.add(packageDescriptor);
    }

    public void handleFile(FileDescriptor mappingFileDescriptor, boolean rootUrl) {
        //        if ( acceptAsMappingFile( mappingFileDescriptor, rootUrl ) ) {
        discoveredMappingFiles.add(mappingFileDescriptor);
        //        }
    }

    public ScanResult toScanResult() {
        return new ScanResultImpl(Collections.unmodifiableSet(discoveredPackages),
                Collections.unmodifiableSet(discoveredClasses), Collections.unmodifiableSet(discoveredMappingFiles));
    }
}
