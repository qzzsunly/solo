package cn.sexycode.util.core.exception;

/**
 * Annotation related exception.
 */
public class AnnotationException extends RuntimeException {
    /**
     * Constructs an AnnotationException using the given message and cause.
     *
     * @param msg   The message explaining the reason for the exception.
     * @param cause The underlying cause.
     */
    public AnnotationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an AnnotationException using the given message.
     *
     * @param msg The message explaining the reason for the exception.
     */
    public AnnotationException(String msg) {
        super(msg);
    }
}
