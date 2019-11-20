package cn.sexycode.util.core.collection;

import java.util.Iterator;

/**
 * @author Gavin King
 */
public final class SingletonIterator<T> implements Iterator<T> {
	private T value;
	private boolean hasNext = true;

	public boolean hasNext() {
		return hasNext;
	}

	public T next() {
		if (hasNext) {
			hasNext = false;
			return value;
		}
		else {
			throw new IllegalStateException();
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public SingletonIterator(T value) {
		this.value = value;
	}

}
