package cn.sexycode.util.core.exception;

/**
 *
 */
public class ClassLoadingException extends RuntimeException {
    public ClassLoadingException(String message) {
        super(message);
    }

    public ClassLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
