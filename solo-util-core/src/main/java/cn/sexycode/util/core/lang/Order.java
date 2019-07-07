package cn.sexycode.util.core.lang;

/**
 * 排序接口
 *
 * @author qzz
 */
public interface Order {
    default int order() {
        return Integer.MIN_VALUE;
    }
}
