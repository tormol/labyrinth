package tbm.util.collections;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import tbm.util.collections.key.KeySet;
import tbm.util.collections.key.KeyObject;

public class ImmutableLeanHashSet<E extends KeyObject<K>, K> extends AbstractSet<E> implements KeySet<E, K> {
	public static final ImmutableLeanHashSet<KeyObject<Object>, Object> emptySet
		= new ImmutableLeanHashSet<>((KeyObject<Object>[])new KeyObject[1], 0, 0);

	@SuppressWarnings("unchecked")
	public static <E extends KeyObject<K>, K> ImmutableLeanHashSet<E, K> copy(Collection<E> col) {
		Objects.requireNonNull(col);
		int size = col.size();
		int power = Integer.highestOneBit(size);
		if (power < size)
			power *= 2;
		E[] elements = col.toArray((E[])new Object[power]);
		int hashes[] = new int[power];
		int shift=0;
		//check for nulls and duplicates, and hash everything
		for (int i=0; i<size; i++) {
			Objects.requireNonNull(elements[i]);
			hashes[i] = elements[i].hashCode();
			for (int ii=0; ii<i; ii++)
				if (hashes[i] == hashes[ii])
					if (elements[i].equals(elements[ii]))
						throw new RuntimeException("Duplicate elements \""+elements[ii]+"\" in collection.");
					else
						return Collections.unmodifiableList(new key.IndexedHashSet<E,K>(col));
				else
					while (hash(hashes, i, shift)  ==  hash(hashes, ii, shift)
					 &&    shift  <  Integer.SIZE / (hashes.length-1))//wrong
						shift++;
		}
		if (shift + 1  ==  Integer.SIZE / (hashes.length-1))
			//return another type
		//sort
		Arrays.sort(elements, new Comparator);
	}

	protected static boolean colission(int[] hashes, int size, int shift) {
		for (int i=1; i<size; i++)
			for (int ii=0; ii<i; ii++)
				if (((hashes[i ]>>shift) & (hashes.length-1))
				 == ((hashes[ii]>>shift) & (hashes.length-1)))
					return true;
		return false; 
	}

	protected static <T> int hash(int[] hashes, int index, int shift) {
		return (hashes[index]>>shift) & (hashes.length-1);
	}



	protected final E[] elements;
	public final int shift, size;
	protected ImmutableLeanHashSet(E[] elements, int size, int shift) {
		this.elements = elements;
		this.size = size;
		this.shift = shift;
	}
	protected int hash(Object o) {
		return (o.hashCode()>>shift) & (elements.length-1);
	}

	public E get(K key) {
		Objects.requireNonNull(key);
		int hash = hash(key);
		if (elements[hash] == null  ||  !elements[hash].getKey().equals(key))
			return null;
		return elements[hash];
	}

	@Override
	public boolean contains(Object o) {
		if (o == null  ||  elements[hash(o)] == null)
			return false;
		int hash = hash(o);
		if (elements[hash] == null)
			return false;
		return o.equals(elements[hash]); 
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>(){
			protected int index=0;
			@Override public boolean hasNext() {
				while (index < elements.length)
					if (elements[index] == null)
						index++;
					else
						return true;
				return false;
			}

			@Override public E next() {
				if (! hasNext())
					return null;
				E e = elements[index];
				index++;
				return e;
			}
		};
	}

	@Override
	public int size() {
		return size;
	}
}
