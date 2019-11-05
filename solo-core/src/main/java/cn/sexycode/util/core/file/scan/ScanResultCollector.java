package cn.sexycode.util.core.file.scan;

public interface ScanResultCollector {
    ScanResult toScanResult();

    void handlePackage(PackageDescriptor packageDescriptor, boolean rootUrl);

    void handleFile(FileDescriptor fileDescriptor, boolean rootUrl);

    void handleClass(ClassDescriptor classDescriptor, boolean rootUrl);
}
