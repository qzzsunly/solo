package cn.sexycode.util.core.cls.internal;

import cn.sexycode.util.core.cls.TypeSwitch;
import cn.sexycode.util.core.cls.XClass;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 *
 */
class JavaXArrayType extends JavaXType {

    public JavaXArrayType(Type type, TypeEnvironment context, JavaReflectionManager factory) {
        super(type, context, factory);
    }

    public boolean isArray() {
        return true;
    }

    public boolean isCollection() {
        return false;
    }

    public XClass getElementClass() {
        return toXClass(getElementType());
    }

    private Type getElementType() {
        //TODO make it a static class for faster performance?
        return new TypeSwitch<Type>() {
            @Override
            public Type caseClass(Class classType) {
                return classType.getComponentType();
            }

            @Override
            public Type caseGenericArrayType(GenericArrayType genericArrayType) {
                return genericArrayType.getGenericComponentType();
            }

            @Override
            public Type defaultCase(Type t) {
                throw new IllegalArgumentException(t + " is not an array type");
            }
        }.doSwitch(approximate());
    }

    public XClass getClassOrElementClass() {
        return getElementClass();
    }

    public Class<? extends Collection> getCollectionClass() {
        return null;
    }

    public XClass getMapKey() {
        return null;
    }

    public XClass getType() {
        Type boundType = getElementType();
        if (boundType instanceof Class) {
            boundType = arrayTypeOf((Class) boundType);
        }
        return toXClass(boundType);
    }

    private Class<? extends Object> arrayTypeOf(Class componentType) {
        return Array.newInstance(componentType, 0).getClass();
    }
}