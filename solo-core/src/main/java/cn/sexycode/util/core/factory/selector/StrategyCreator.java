package cn.sexycode.util.core.factory.selector;

/**
 * @author Steve Ebersole
 */
public interface StrategyCreator<T> {
	T create(Class<? extends T> strategyClass);
}
