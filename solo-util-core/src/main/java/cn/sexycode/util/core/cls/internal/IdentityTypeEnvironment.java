package cn.sexycode.util.core.cls.internal;

import java.lang.reflect.Type;

/**
 * Substitutes a <code>Type</code> for itself.
 */
public class IdentityTypeEnvironment implements TypeEnvironment {

    public static final TypeEnvironment INSTANCE = new IdentityTypeEnvironment();

    private IdentityTypeEnvironment() {
    }

    public Type bind(Type type) {
        return type;
    }

    public String toString() {
        return "{}";
    }
}
