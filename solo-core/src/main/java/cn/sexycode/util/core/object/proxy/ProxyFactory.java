package cn.sexycode.util.core.object.proxy;

import java.lang.reflect.InvocationHandler;

public interface ProxyFactory {
    Object createProxy(Object target, InvocationHandler handler);
}
