package cn.sexycode.util.core.cls.internal;

import cn.sexycode.util.core.cls.*;
import cn.sexycode.util.core.exception.ClassLoadingException;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The factory for all the objects in this package.
 */
public class JavaReflectionManager implements ReflectionManager, MetadataProviderInjector {
    private MetadataProvider metadataProvider;

    private ClassLoaderDelegate classLoaderDelegate = StandardClassLoaderDelegateImpl.INSTANCE;

    @Override
    public MetadataProvider getMetadataProvider() {
        if (metadataProvider == null) {
            setMetadataProvider(new JavaMetadataProvider());
        }
        return metadataProvider;
    }

    @Override
    public void setMetadataProvider(MetadataProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
    }

    @Override
    public void injectClassLoaderDelegate(ClassLoaderDelegate delegate) {
        if (delegate == null) {
            this.classLoaderDelegate = StandardClassLoaderDelegateImpl.INSTANCE;
        } else {
            this.classLoaderDelegate = delegate;
        }
    }

    @Override
    public ClassLoaderDelegate getClassLoaderDelegate() {
        return classLoaderDelegate;
    }

    private static class TypeKey extends Pair<Type, TypeEnvironment> {
        TypeKey(Type t, TypeEnvironment context) {
            super(t, context);
        }
    }

    private static class MemberKey extends Pair<Member, TypeEnvironment> {
        MemberKey(Member member, TypeEnvironment context) {
            super(member, context);
        }
    }

    private final Map<TypeKey, JavaXClass> xClasses = new HashMap<TypeKey, JavaXClass>();

    private final Map<Package, JavaXPackage> packagesToXPackages = new HashMap<Package, JavaXPackage>();

    private final Map<MemberKey, JavaXProperty> xProperties = new HashMap<MemberKey, JavaXProperty>();

    private final Map<MemberKey, JavaXMethod> xMethods = new HashMap<MemberKey, JavaXMethod>();

    private final TypeEnvironmentFactory typeEnvs = new TypeEnvironmentFactory();

    @Override
    public XClass toXClass(Class clazz) {
        return toXClass(clazz, IdentityTypeEnvironment.INSTANCE);
    }

    @Override
    public Class toClass(XClass xClazz) {
        if (!(xClazz instanceof JavaXClass)) {
            throw new IllegalArgumentException("XClass not coming from this ReflectionManager implementation");
        }
        return (Class) ((JavaXClass) xClazz).toAnnotatedElement();
    }

    @Override
    public Method toMethod(XMethod xMethod) {
        if (!(xMethod instanceof JavaXMethod)) {
            throw new IllegalArgumentException("XMethod not coming from this ReflectionManager implementation");
        }
        return (Method) ((JavaXAnnotatedElement) xMethod).toAnnotatedElement();
    }

    @Override
    public XClass classForName(String name) throws ClassLoadingException {
        return toXClass(getClassLoaderDelegate().classForName(name));
    }

    @Override
    public XPackage packageForName(String packageName) throws ClassNotFoundException {
        return getXAnnotatedElement(getClassLoaderDelegate().classForName(packageName + ".package-info").getPackage());
    }

    XClass toXClass(Type t, final TypeEnvironment context) {
        return new TypeSwitch<XClass>() {
            @Override
            public XClass caseClass(Class classType) {
                TypeKey key = new TypeKey(classType, context);
                JavaXClass result = xClasses.get(key);
                if (result == null) {
                    result = new JavaXClass(classType, context, JavaReflectionManager.this);
                    xClasses.put(key, result);
                }
                return result;
            }

            @Override
            public XClass caseParameterizedType(ParameterizedType parameterizedType) {
                return toXClass(parameterizedType.getRawType(), typeEnvs.getEnvironment(parameterizedType, context));
            }
        }.doSwitch(context.bind(t));
    }

    XPackage getXAnnotatedElement(Package pkg) {
        JavaXPackage xPackage = packagesToXPackages.get(pkg);
        if (xPackage == null) {
            xPackage = new JavaXPackage(pkg, this);
            packagesToXPackages.put(pkg, xPackage);
        }
        return xPackage;
    }

    XProperty getXProperty(Member member, TypeEnvironment context) {
        MemberKey key = new MemberKey(member, context);
        //FIXME get is as expensive as create most time spent in hashCode and equals
        JavaXProperty xProperty = xProperties.get(key);
        if (xProperty == null) {
            xProperty = JavaXProperty.create(member, context, this);
            xProperties.put(key, xProperty);
        }
        return xProperty;
    }

    XMethod getXMethod(Member member, TypeEnvironment context) {
        MemberKey key = new MemberKey(member, context);
        //FIXME get is as expensive as create most time spent in hashCode and equals
        JavaXMethod xMethod = xMethods.get(key);
        if (xMethod == null) {
            xMethod = JavaXMethod.create(member, context, this);
            xMethods.put(key, xMethod);
        }
        return xMethod;
    }

    TypeEnvironment getTypeEnvironment(final Type t) {
        return new TypeSwitch<TypeEnvironment>() {
            @Override
            public TypeEnvironment caseClass(Class classType) {
                return typeEnvs.getEnvironment(classType);
            }

            @Override
            public TypeEnvironment caseParameterizedType(ParameterizedType parameterizedType) {
                return typeEnvs.getEnvironment(parameterizedType);
            }

            @Override
            public TypeEnvironment defaultCase(Type type) {
                return IdentityTypeEnvironment.INSTANCE;
            }
        }.doSwitch(t);
    }

    public JavaXType toXType(TypeEnvironment context, Type propType) {
        Type boundType = toApproximatingEnvironment(context).bind(propType);
        if (TypeUtils.isArray(boundType)) {
            return new JavaXArrayType(propType, context, this);
        }
        if (TypeUtils.isCollection(boundType)) {
            return new JavaXCollectionType(propType, context, this);
        }
        if (TypeUtils.isSimple(boundType)) {
            return new JavaXSimpleType(propType, context, this);
        }
        throw new IllegalArgumentException("No PropertyTypeExtractor available for type void ");
    }

    @Override
    public boolean equals(XClass class1, Class class2) {
        if (class1 == null) {
            return class2 == null;
        }
        return ((JavaXClass) class1).toClass().equals(class2);
    }

    public TypeEnvironment toApproximatingEnvironment(TypeEnvironment context) {
        return typeEnvs.toApproximatingEnvironment(context);
    }

    @Override
    public AnnotationReader buildAnnotationReader(AnnotatedElement annotatedElement) {
        return getMetadataProvider().getAnnotationReader(annotatedElement);
    }

    @Override
    public Map getDefaults() {
        return getMetadataProvider().getDefaults();
    }

}
