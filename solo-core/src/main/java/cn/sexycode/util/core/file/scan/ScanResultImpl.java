package cn.sexycode.util.core.file.scan;

import java.util.Set;



/**
* @author Steve Ebersole
*/
public class ScanResultImpl implements ScanResult {
	private final Set<PackageDescriptor> packageDescriptorSet;
	private final Set<ClassDescriptor> classDescriptorSet;
	private final Set<FileDescriptor> mappingFileSet;

	public ScanResultImpl(
			Set<PackageDescriptor> packageDescriptorSet,
			Set<ClassDescriptor> classDescriptorSet,
			Set<FileDescriptor> mappingFileSet) {
		this.packageDescriptorSet = packageDescriptorSet;
		this.classDescriptorSet = classDescriptorSet;
		this.mappingFileSet = mappingFileSet;
	}

	@Override
	public Set<PackageDescriptor> getLocatedPackages() {
		return packageDescriptorSet;
	}

	@Override
	public Set<ClassDescriptor> getLocatedClasses() {
		return classDescriptorSet;
	}

	@Override
	public Set<FileDescriptor> getLocatedMappingFiles() {
		return mappingFileSet;
	}
}
