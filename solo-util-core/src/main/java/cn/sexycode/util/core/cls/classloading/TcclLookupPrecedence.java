package cn.sexycode.util.core.cls.classloading;

/**
 * Defines when the lookup in the current thread context {@link ClassLoader} should be
 * done according to the other ones.
 *
 * @author Cédric Tabin
 */
public enum TcclLookupPrecedence {
    /**
     * The current thread context {@link ClassLoader} will never be used during
     * the class lookup.
     */
    NEVER,

    /**
     * The class lookup will be done in the thread context {@link ClassLoader} prior
     * to the other {@code ClassLoader}s.
     */
    BEFORE,

    /**
     * The class lookup will be done in the thread context {@link ClassLoader} if
     * the former hasn't been found in the other {@code ClassLoader}s.
     * This is the default value.
     */
    AFTER
}
