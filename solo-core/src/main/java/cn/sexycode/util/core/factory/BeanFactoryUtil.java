package cn.sexycode.util.core.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qinzaizhen
 */
public class BeanFactoryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanFactoryUtil.class);

    private static BeanFactory beanFactory = new BeanFactory() {
        @Override
        public <T> T getBean(Class<T> clazz) {
            LOGGER.error("请通过set方法初始化BeanFactory");
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBean(String beanName) {
            LOGGER.error("请通过set方法初始化BeanFactory");
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBean(String beanName, Class<T> clazz) {
            LOGGER.error("请通过set方法初始化BeanFactory");
            throw new UnsupportedOperationException();
        }
    };

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static void setBeanFactory(BeanFactory beanFactory) {
        BeanFactoryUtil.beanFactory = beanFactory;
    }
}
