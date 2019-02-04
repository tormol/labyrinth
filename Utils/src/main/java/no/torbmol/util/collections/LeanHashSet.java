/* Copyright 2016 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util.collections;
import static java.util.Objects.requireNonNull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class LeanHashSet<E> extends LeanHash<E> implements SetWithGet<E> {
	public static <E> LeanHashSet<E> copy(Collection<E> c) {
		if (c instanceof LeanHashSet)
			return ((LeanHashSet<E>)c).clone();
		return new LeanHashSet<E>(c);
	}


	public LeanHashSet(int initialCapacity, float ratio) {
		super(initialCapacity, ratio);
	}

	protected LeanHashSet(E[] elements, long[] buckets) {
		super(elements, buckets);
	}

	/**If c might be a LeanHashSet, use LeanHashSet.copy(c)*/
	public LeanHashSet(Collection<E> c) {
		super(requireNonNull(c).size(), default_ratio);
		addAll(c);
	}


	@Override protected final int ew() {
		return 1;
	}


	@Override public boolean add(E e) {
		long hash_index = indexOf(e);
		if (index(hash_index) != -1)
			return false;
		add_new(e, hash(hash_index));
		return true;
	}

	@Override public E get(Object obj) {
		//ok to throw NullPointerException if obj == null
		int index = index(indexOf(obj));
		if (index == -1)
			return null;
		return elements[index];
	}

	@Override public boolean contains(Object obj) {
		if (obj == null)
			return false;
		return index(indexOf(obj)) != -1;
	}

	@Override public boolean remove(Object obj) {
		return remove_element(obj) != -1;
	}


	@Override public boolean addAll(Collection<? extends E> col) {
		boolean modified = false;
		for (E e : col)
			modified |= add(e);
		return modified;
	}

	@Override public boolean containsAll(Collection<?> col) {
		for (Object e : col)
			if (! this.contains(e))
				return false;
		return true;
	}

	@Override public boolean removeAll(Collection<?> col) {
		boolean modified = false;
		for (Object e : col)
			modified |= remove(e);
		return modified;
	}

	@Override public boolean retainAll(Collection<?> col) {
		boolean modified = false;
		for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
			if (! col.contains(elements[index(b_i)])) {
				remove_index(hash(elements[index(b_i)]), index(b_i));
				modified = true;
			}
		return modified;
	}


	@Override public ExtendedIterator<E> iterator() {
		return new Iter<E>();
	}

	@Override public boolean equals(Object o) {
		if (o instanceof LeanHashSet)
			return super.equals(o);
		if (! (o instanceof Set))
			return false;
		Set<?> other = (Set<?>)o;
		return this.size() == other.size()  &&  this.containsAll(other);
	}

	@Override public LeanHashSet<E> clone() {
		LeanHash<E> clone = super.clone();
		if (clone == null)
			clone = new LeanHashSet<>(elements.clone(), buckets.clone());
		return (LeanHashSet<E>) clone;
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (E el : elements)
			if (el != null)
				sb.append(el).append(',').append(' ');
		if (sb.length() > 1)//more than zero elements.
			sb.delete(sb.length()-2, sb.length());//remove last ", "
		sb.append(']');
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override public <T> T[] toArray(Class<T[]> ofType) {
		T[] copy = (T[]) Array.newInstance(ofType.getComponentType(), size());
		int put = copy.length;//FIXME: why opposite order?
		for (E el : elements)
			if (el != null) {
				put--;
				copy[put] = (T)el;
			}
		return copy;
	}

	@SuppressWarnings("unchecked")
	@Override public <T> T[] toArray(T[] copy) {
		requireNonNull(copy);
		int put = 0;//elements must be from start of array
		int from = elements.length;//FIXME: why opposite order?
		while (from > 0) {
			from--;
			if (elements[from] != null) {
				//then add to copy
				if (put == copy.length) {
					//array is too small
					int elements_left = 1;//elements[from]
					for (int i=0;  i<from;  i++)
						if (elements[i] != null)
							elements_left++;
					copy = Arrays.copyOf(copy, copy.length+elements_left);
				}
				copy[put] = (T)elements[from];
				put++;
			}
		}
		if (put < copy.length)
			Arrays.fill(copy, put, copy.length, null);
		return copy;
	}


	private static final long serialVersionUID = 1L;
}
