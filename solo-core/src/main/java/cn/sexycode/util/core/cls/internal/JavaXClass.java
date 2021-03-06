package cn.sexycode.util.core.cls.internal;

import cn.sexycode.util.core.cls.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
class JavaXClass extends JavaXAnnotatedElement implements XClass {

    private final TypeEnvironment context;

    private final Class clazz;

    public JavaXClass(Class clazz, TypeEnvironment env, JavaReflectionManager factory) {
        super(clazz, factory);
        this.clazz = clazz; //optimization
        this.context = env;
    }

    public String getName() {
        return toClass().getName();
    }

    public XClass getSuperclass() {
        return getFactory().toXClass(toClass().getSuperclass(),
                CompoundTypeEnvironment.create(getTypeEnvironment(), getFactory().getTypeEnvironment(toClass())));
    }

    public XClass[] getInterfaces() {
        Class[] classes = toClass().getInterfaces();
        int length = classes.length;
        XClass[] xClasses = new XClass[length];
        if (length != 0) {
            TypeEnvironment environment = CompoundTypeEnvironment
                    .create(getTypeEnvironment(), getFactory().getTypeEnvironment(toClass()));
            for (int index = 0; index < length; index++) {
                xClasses[index] = getFactory().toXClass(classes[index], environment);
            }
        }
        return xClasses;
    }

    public boolean isInterface() {
        return toClass().isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(toClass().getModifiers());
    }

    public boolean isPrimitive() {
        return toClass().isPrimitive();
    }

    public boolean isEnum() {
        return toClass().isEnum();
    }

    private List<XProperty> getDeclaredFieldProperties(Filter filter) {
        List<XProperty> result = new LinkedList<XProperty>();
        for (Field f : toClass().getDeclaredFields()) {
            if (ReflectionUtil.isProperty(f, getTypeEnvironment().bind(f.getGenericType()), filter)) {
                result.add(getFactory().getXProperty(f, getTypeEnvironment()));
            }
        }
        return result;
    }

    private List<XProperty> getDeclaredMethodProperties(Filter filter) {
        List<XProperty> result = new LinkedList<XProperty>();
        for (Method m : toClass().getDeclaredMethods()) {
            if (ReflectionUtil.isProperty(m, getTypeEnvironment().bind(m.getGenericReturnType()), filter)) {
                result.add(getFactory().getXProperty(m, getTypeEnvironment()));
            }
        }
        return result;
    }

    public List<XProperty> getDeclaredProperties(String accessType) {
        return getDeclaredProperties(accessType, XClass.DEFAULT_FILTER);
    }

    public List<XProperty> getDeclaredProperties(String accessType, Filter filter) {
        if (accessType.equals(ACCESS_FIELD)) {
            return getDeclaredFieldProperties(filter);
        }
        if (accessType.equals(ACCESS_PROPERTY)) {
            return getDeclaredMethodProperties(filter);
        }
        throw new IllegalArgumentException("Unknown access type " + accessType);
    }

    public List<XMethod> getDeclaredMethods() {
        List<XMethod> result = new LinkedList<XMethod>();
        for (Method m : toClass().getDeclaredMethods()) {
            result.add(getFactory().getXMethod(m, getTypeEnvironment()));
        }
        return result;
    }

    public Class<?> toClass() {
        return clazz;
    }

    public boolean isAssignableFrom(XClass c) {
        return toClass().isAssignableFrom(((JavaXClass) c).toClass());
    }

    boolean isArray() {
        return toClass().isArray();
    }

    TypeEnvironment getTypeEnvironment() {
        return context;
    }

    @Override
    public String toString() {
        return getName();
    }
}