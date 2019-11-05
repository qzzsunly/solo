package cn.sexycode.util.core.factory;

public interface BeanFactory {
    <T> T getBean(Class<T> clazz);
}
