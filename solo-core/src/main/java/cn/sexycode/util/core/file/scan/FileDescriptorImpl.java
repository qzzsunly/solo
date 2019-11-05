package cn.sexycode.util.core.file.scan;

import java.io.InputStream;

/**
 * @author Steve Ebersole
 */
public class FileDescriptorImpl implements FileDescriptor {
	private final String name;
	private final InputStream streamAccess;

	public FileDescriptorImpl(String name, InputStream streamAccess) {
		this.name = name;
		this.streamAccess = streamAccess;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream getStream() {
		return streamAccess;
	}

}
