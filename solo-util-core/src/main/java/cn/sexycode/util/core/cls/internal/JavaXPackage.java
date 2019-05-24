package cn.sexycode.util.core.cls.internal;

import cn.sexycode.util.core.cls.XPackage;

/**
 *
 */
class JavaXPackage extends JavaXAnnotatedElement implements XPackage {

    public JavaXPackage(Package pkg, JavaReflectionManager factory) {
        super(pkg, factory);
    }

    public String getName() {
        return ((Package) toAnnotatedElement()).getName();
    }
}
