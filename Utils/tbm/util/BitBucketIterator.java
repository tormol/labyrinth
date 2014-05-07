package tbm.util;

import java.util.ListIterator;

public class BitBucketIterator<E> implements ListIterator<E> {
	@Override//Iterator
	public boolean hasNext() {
		return false;
	}
	@Override//Iterator
	public E next() {
		return null;
	}
	@Override//Iterator
	public void remove()
		{}
	@Override//ListIterator
	public void add(E e)
		{}
	@Override//ListIterator
	public boolean hasPrevious() {
		return false;
	}
	@Override//ListIterator
	public int nextIndex() {
		return 0;
	}
	@Override//ListIterator
	public E previous() {
		return null;
	}
	@Override//ListIterator
	public int previousIndex() {
		return -1;
	}
	@Override//ListIterator
	public void set(E e) {
		throw new UnsupportedOperationException();
	}
}
