package cn.sexycode.util.core.cls;

import java.lang.reflect.*;

/**
 * A visitor for the <code>java.lang.reflect.Type</code> hierarchy.
 */
public class TypeSwitch<T> {

    public final T doSwitch(Type type) {
        if (type instanceof Class) {
            return caseClass((Class) type);
        }
        if (type instanceof GenericArrayType) {
            return caseGenericArrayType((GenericArrayType) type);
        }
        if (type instanceof ParameterizedType) {
            return caseParameterizedType((ParameterizedType) type);
        }
        if (type instanceof TypeVariable) {
            return caseTypeVariable((TypeVariable) type);
        }
        if (type instanceof WildcardType) {
            return caseWildcardType((WildcardType) type);
        }
        return defaultCase(type);
    }

    public T caseWildcardType(WildcardType wildcardType) {
        return defaultCase(wildcardType);
    }

    public T caseTypeVariable(TypeVariable typeVariable) {
        return defaultCase(typeVariable);
    }

    public T caseClass(Class classType) {
        return defaultCase(classType);
    }

    public T caseGenericArrayType(GenericArrayType genericArrayType) {
        return defaultCase(genericArrayType);
    }

    public T caseParameterizedType(ParameterizedType parameterizedType) {
        return defaultCase(parameterizedType);
    }

    public T defaultCase(Type t) {
        return null;
    }
}
