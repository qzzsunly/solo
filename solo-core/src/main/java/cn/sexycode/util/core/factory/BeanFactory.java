package cn.sexycode.util.core.factory;

public interface BeanFactory {
    <T> T getBean(Class<T> clazz);
    <T> T getBean(String beanName);
    <T> T getBean(String beanName, Class<T> clazz);
}
