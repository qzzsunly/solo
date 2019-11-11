package cn.sexycode.util.core.object.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * @author qinzaizhen
 */
public interface CglibProxyFactory extends ProxyFactory{
    /**
     * @param target
     * @param handler
     * @return
     */
    @Override
    default  Object createProxy(Object target, java.lang.reflect.InvocationHandler handler){
        Enhancer e=new Enhancer();
        //2.2设置增强器的类加载器
        e.setClassLoader(target.getClass().getClassLoader());
        //2.3设置代理对象父类类型
        e.setSuperclass(target.getClass());
        //2.4设置回调函数
        e.setCallback((InvocationHandler) handler::invoke);
        return e.create();
    }
}
