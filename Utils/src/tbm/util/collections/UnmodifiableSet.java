package tbm.util.collections;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public abstract class UnmodifiableSet<E> extends AbstractSet<E> implements SetWithGet<E>, Serializable {
	/**is mutable to allow users to profile and set a better value for their uses.*/
	protected static int linear_max_size = 5;//a guesstimate

	private static <E> UnmodifiableSet<E> create(E[] elements, boolean fromSet, boolean canContainNull) {
		if (elements.length > linear_max_size)
			try {
				UnmodifiableHashSet<E> set = Bits.attemt(elements, fromSet);
				if (set != null)
					return set;
				return new UnmodifiableStartTableHashSet<E>(elements, fromSet);	
			} catch (NullPointerException npe) {
				if ( !canContainNull)
					throw new IllegalArgumentException("null element");
			}
		return new UnmodifiableSmallSet<E>(elements, fromSet);
	}

	@SuppressWarnings("unchecked")//only way to transfer generic
	public static <E> UnmodifiableSet<E> from(Collection<E> c, boolean canContainNull) {
		return create((E[])c.toArray(), c instanceof Set<?>, canContainNull);
	}
	public static <E> UnmodifiableSet<E> from(E[] elements, boolean canContainNull) {
		return create(elements, false, canContainNull);
	}


	/**Create an UnmodifiableSet with linear search*/@SafeVarargs//constructor accepts Object[]
	//an E... constructor would take a E[] array, and Collection<E> and not be the expected type
	public static <E> UnmodifiableSet<E> small(E... elements) {
		return new UnmodifiableSmallSet<E>(elements, false);
	}




	  ////////////////////
	 //The actual class//
	////////////////////
	protected final Object[] elements;
	protected abstract int indexOf(Object o);

	protected UnmodifiableSet(Object[] elements) {
		this.elements = elements;
	}


	//Is not final so a implementation can use 2^n-1 sized arrays or store null element by setting a flag 
	@Override public int size() {
		return elements.length;
	}

	@Override public final boolean contains(Object o) {
		return indexOf(o) >= 0;//UnmodifiableSortedSet.indexOf()s Array.binarySearch() can return any negative value
	}

	@SuppressWarnings("unchecked")//E.. and .toArray() might create an Object[], but all elements are E
	@Override public final E get(Object o) {
		int index = indexOf(o);
		if (index >= 0)
			return (E)elements[index];
		return null;
	}


	public <T> T[] toArray(Class<T[]> ofType) {
		return Arrays.copyOf(elements, elements.length, ofType);
	}

	@Override public final Object[] toArray() {
		return toArray(Object[].class);
	}

	@SuppressWarnings("unchecked")
	@Override public <T> T[] toArray(T[] array) {
		if (array.length < size())
			return toArray((Class<T[]>)array.getClass());
		System.arraycopy(elements, 0, array, 0, size());
		Arrays.fill(array, size(), array.length, null);
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override public arrayIterators.Unmodifiable<E> iterator() {
		return new arrayIterators.Unmodifiable<E>((E[])elements);
	}


	//mutation methods already implemented in AbstractSet, but now they're final
	/**@throws UnsupportedOperationException if called
	 * @deprecated Always throws UnsupportedOperationException*/@Deprecated
	@Override public final boolean add(E e) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**@throws UnsupportedOperationException if called
	 * @deprecated Always throws UnsupportedOperationException*/@Deprecated
	@Override public final boolean remove(Object o) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	//TODO: make more methods final?

	private static final long serialVersionUID = 1L;




	  ///////////////////////////
	 //private implementations//
	///////////////////////////

	/**A unmodifiable Set where linear search is used in contains() and to find duplicates.
	 *Should only be used for small sets (*/
	private static class UnmodifiableSmallSet<E> extends UnmodifiableSet<E> {
		protected UnmodifiableSmallSet(E[] elements, boolean fromSet) {
			super(elements);
			if (! fromSet) {
				boolean hasNull = false;
				for (int i=0; i<elements.length; i++)
					if (elements[i] != null)
						for (int ii=0; ii<i; ii++) {
							if (elements[i].equals(elements[ii]))
								throw new IllegalArgumentException("multiple "+elements[i]+'s');
						}//keeps else if outside the loop
					else if (hasNull)
						throw new IllegalArgumentException("multiple nulls");
					else
						hasNull = true;
			}
		}
		@Override protected int indexOf(Object o) {
			for (int i=0; i<elements.length; i++)
				if (o == elements[i]  ||  (o != null  &&  o.equals(elements[i])))
					return i;
			return -1;
		} 
		private static final long serialVersionUID = 1L;
	}


	private static class Bits<E> extends UnmodifiableHashSet<E> {
		public static <E> UnmodifiableHashSet<E> attemt(E[] elements, boolean fromSet) {
			// TODO Auto-generated method stub
			return null;
		}

		protected final int addToHash;
		protected final byte shiftLeft, shiftRight;
		protected Bits(E[] elements, int addToHash, byte shiftLeft, byte shiftRight, boolean fromSet) {
			super(elements);
			this.addToHash = addToHash;
			this.shiftLeft = shiftLeft;
			this.shiftRight = shiftRight;
			if ( !fromSet)
				checkForDuplicates();
		}
		@Override protected int hash(Object o) {
			return ((o.hashCode() + addToHash) << shiftLeft) >> shiftRight;
		}
		private static final long serialVersionUID = 1L;
	}
}
