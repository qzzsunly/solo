package cn.sexycode.util.core.file.scan;

import java.io.InputStream;

/**
 * @author Steve Ebersole
 */
public class ClassDescriptorImpl implements ClassDescriptor {
    private final String name;

    private final Categorization categorization;

    private final InputStream streamAccess;

    public ClassDescriptorImpl(String name, Categorization categorization, InputStream streamAccess) {
        this.name = name;
        this.categorization = categorization;
        this.streamAccess = streamAccess;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Categorization getCategorization() {
        return categorization;
    }

    @Override
    public InputStream getStream() {
        return streamAccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClassDescriptorImpl that = (ClassDescriptorImpl) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
