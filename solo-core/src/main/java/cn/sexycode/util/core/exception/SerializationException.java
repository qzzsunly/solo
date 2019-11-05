package cn.sexycode.util.core.exception;

/**
 * Thrown when a property cannot be serializaed/deserialized
 */
public class SerializationException extends RuntimeException {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable root) {
        super(message, root);
    }

}
