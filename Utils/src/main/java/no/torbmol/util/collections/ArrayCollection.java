/* Copyright 2016 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util.collections;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import no.torbmol.util.collections.randomAccessIterators.ModifiableSkipEmpty;

/**A collection, nothing more, nothing less.
 * Supports null elements and doesn't use equals() or hashCode().*/
public class ArrayCollection<E> extends AbstractCollection<E> implements Serializable, CollectionWithToArrayType<E> {
	/**is also used by other classes in no.torbmol.util.collections
	 *.toString() returns {@code "empty"}
	 *.equals() returns {@code false}*/
	protected static final Object empty = new Object() {
		@Override public String toString() {
			return "empty";
		}
		@Override public boolean equals(Object o) {
			return false;
		}
	};


	protected static final int default_size = 8;


	protected Object[] elements;
	protected int size;
	/**index of a known unused slot, or -1 if unknown*/
	protected int anEmpty;

	protected ArrayCollection(Object[] elements, int size, int anEmpty) {
		if (elements.getClass() != Object[].class) {
			this.elements = new Object[elements.length];
			System.arraycopy(elements, 0, this.elements, 0, elements.length);
		} else
			this.elements = elements;
		this.size = size;
		this.anEmpty = anEmpty;
	}
	public ArrayCollection(Collection<E> c) {
		elements = c.toArray();
		size = elements.length;
		anEmpty = -1;
	}
	@SafeVarargs//I actually want an Object[]
	public ArrayCollection(E... elements) {
		this(elements, elements.length, -1);
	}
	public ArrayCollection() {
		this(new Objects[default_size],  0,  default_size-1);
		Arrays.fill(elements, empty);
	}


	/**uses {@code .equal()}*/
	protected int indexOf(int begin, Object o) {
		if (o == null)
			return indexOfRef(begin, o);
		while (begin < elements.length)
			if (o.equals(elements[begin]))
				return begin;
			else
				begin++;
		return -1;
	}
	/**uses {@code ==}*/
	protected int indexOfRef(int begin, Object o) {
		while (begin < elements.length)
			if (elements[begin] == o)
				return begin;
			else
				begin++;
		return -1;
	}
	public boolean resize(int free) {
		if (elements.length < 4  ||  elements.length-size == free)
			return false;
		elements = toArray(free);
		anEmpty = free-1;
		return true;
	}

	@Override
	public int size() {
		return size;
	}
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(0, o) != -1;
	}
	public boolean containsRef(Object o) {
		return indexOfRef(0, o) != -1;
	}

	@Override
	public boolean add(E e) {
		if (anEmpty != -1) {
			elements[anEmpty] = e;
			anEmpty = -1;
		} else if (size == elements.length) {
			int new_size = 1 + (size * 3) / 2;//add one to ensure growth when size < 2
			int diff = new_size - size;
			elements = toArray(diff);
			elements[diff-1] = e;
			anEmpty = diff - 2;
		} else
			elements[indexOfRef(0, empty)] = e;
		size++;
		return true;
	}

	protected boolean remove(int index) {
		if (index == -1)
			return false;
		elements[index] = empty;
		size--;
		if (anEmpty < index)
			anEmpty = index;
		//TODO: shrink
		return true;
	}
	@Override
	public boolean remove(Object o) {
		return remove(indexOf(0, o));
	}
	public int removeCompletely(Object o) {
		int i=0, next=indexOf(0, o);
		while (next != -1) {
			remove(next);
			i++;
			next = indexOf(next+1, o);
		}
		return i;
	}
	@Override
	public boolean removeAll(Collection<?> col) {
		Objects.requireNonNull(col);
		boolean modified = false;
		for (Object o : col)
			if (removeCompletely(o) > 0)
				modified = true;
		return modified;
	}

	public boolean removeRef(Object o) {
		return remove(indexOfRef(0, o));
	}
	public int removeRefCompletely(Object o) {
		int i=0, next=indexOf(0, o);
		while (removeRef(next)) {
			i++;
			next = indexOf(next+1, o);
		}
		return i;
	}
	public boolean removeAllRef(Collection<?> col) {
		Objects.requireNonNull(col);
		boolean modified = false;
		for (Object oe : col)
			if (removeRefCompletely(oe) > 0)
				modified = true;
		return modified;
	}

	@Override
	public void clear() {
		if (elements.length > 2*default_size)
			elements = new Object[default_size];
		Arrays.fill(elements, empty);
		anEmpty = elements.length-1;
		size = 0;
	}


	//Iterable
	@SuppressWarnings("unchecked")
	@Override public ModifiableSkipEmpty<E> iterator() {//might work
		return (ModifiableSkipEmpty<E>)new ModifiableSkipEmpty<Object>() {
			@Override protected    int	    maxIndex()                   	{return elements.length;}
			@Override protected Object	    getIndex(int index)          	{return elements[index];}
			@Override protected   void	    setIndex(int index, Object e)	{elements[index] = e;}
			@Override protected   void	 removeIndex(int index)          	{elements[index] = empty;  size--;}
			@Override protected Object	emptyElement()                   	{return empty;}
		};
	}

	@SuppressWarnings("unchecked")
	@Override public void forEach(Consumer<? super E> consumer) {
		for (Object o : elements)
			if (o != empty)
				consumer.accept((E)o);
	}


	/**move all emptys to start of elements
	 *@return number of <tt>empty</tt>s*/
	protected int pack() {
		int emptys = elements.length - size();

		//use anEmpty
		int from = 0;
		if (anEmpty >= emptys) {//then emptys > 0
			while (elements[from] == empty)
				from++;
			elements[anEmpty] = elements[from];
			from++;
		}
		anEmpty = emptys - 1;

		//move
		for (int fillIn = emptys;  from < emptys;  from++)
			if (elements[from] != empty) {
				while (elements[fillIn] != empty)
					fillIn++;
				elements[fillIn] = elements[from];
				elements[from] = empty;
			}
		return emptys;
	}

	private <T> T[] toArray(int free, Class<T[]> type) throws IllegalArgumentException {
		if (free < 0)
			throw new IllegalArgumentException("free cannot be less than zero, but is "+free+'.');
		int emptys = pack();
		@SuppressWarnings("unchecked")
		T[] new_elements = (T[]) Array.newInstance(type.getComponentType(), size()+free);
		Arrays.fill(new_elements, 0, free, empty);
		System.arraycopy(elements, emptys, new_elements, free, size());
		return new_elements;
	}
	@SuppressWarnings("unchecked")
	protected E[] toArray(int free) {
		return toArray(free, (Class<E[]>)elements.getClass());
	}
	@Override public Object[] toArray() {
		return toArray(0, Object[].class);
	}
	@Override public <T> T[] toArray(Class<T[]> type) {
		return toArray(0, type);
	}

	private static final long serialVersionUID = 1;
}
