package tbm.util;

import java.util.Iterator;
//import java.util.ListIterator;

public class ArrayIterator<E> implements Iterable<E>, Iterator<E> {
	private final E[] array;
	private int index=-1;

	@SuppressWarnings("unchecked")
	public ArrayIterator(E[] array) {
		if (array == null)
			this.array = (E[])new Object[0];
		else
			this.array = array;
	}

	@Override//Iterable
	public Iterator<E> iterator() {
		return this;
	}

	@Override//Iterator
	public boolean hasNext() {
		return (index+1 < array.length);
	}
	@Override//Iterator
	public E next() {
		index++;
		if (index >= array.length)
			return null;
		return array[index]; 
	}
	@Override//Iterator
	public void remove() {
		throw new UnsupportedOperationException();		
	}

	//@Override//ListIterator
	public E set(E e) {
		if (index == -1  ||  index >= array.length)
			throw new IllegalStateException();
		array[index] = e;
		return e;
	}

	/*
	@Override//ListIterator
	public void add(E arg0) {
		throw new UnsupportedOperationException();
	}
	@Override//ListIterator
	public boolean hasPrevious() {
		return (index>0);
	}
	@Override//ListIterator
	public int nextIndex() {
		if (index==array.length)
			return array.length;
		return index+1;
	}
	@Override//ListIterator
	public E previous() {
		if (index==-1)
			return null;
		
	}
	@Override//ListIterator
	public int previousIndex() {
		return index-1;
	}*/
}
