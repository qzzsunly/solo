package cn.sexycode.util.core.exception;

/**
 * Indicates that an expected getter or setter method could not be
 * found on a class.
 */
public class PropertyNotFoundException extends RuntimeException {
    /**
     * Constructs a PropertyNotFoundException given the specified message.
     *
     * @param message A message explaining the exception condition
     */
    public PropertyNotFoundException(String message) {
        super(message);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
