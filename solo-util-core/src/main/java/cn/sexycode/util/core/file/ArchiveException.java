package cn.sexycode.util.core.file;

/**
 * Indicates a problem accessing or visiting the archive
 *
 * @author Steve Ebersole
 */
public class ArchiveException extends RuntimeException {
    /**
     * Constructs an ArchiveException
     *
     * @param message Message explaining the exception condition
     */
    public ArchiveException(String message) {
        super(message);
    }

    /**
     * Constructs an ArchiveException
     *
     * @param message Message explaining the exception condition
     * @param cause   The underlying cause
     */
    public ArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
