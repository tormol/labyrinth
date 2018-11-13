package tbm.util.collections;
import java.util.Arrays;

/**A set that support any element except null, and uses a hash function for contains()*/
public abstract class UnmodifiableHashSet<E> extends UnmodifiableSet<E> {
	/**0 <= hash < elements.length
	 *@param o not null*/
	protected abstract int hash(Object o);

	protected UnmodifiableHashSet(Object[] elements, boolean fromSet) {
		super(elements, !fromSet);
	}

	/**Check for duplicates by setting an elementt to ArrayCollection.empty and see if the set still contains() the removed element.
	 *Makes a copy of elements if it's not an Object[]
	 *@throws IllegalArgumentException if one elements equals another*/
	@Override protected void checkForDuplicates() throws IllegalArgumentException {
		Object[] elements = this.elements;
		if (elements.getClass() == Object[].class)
			elements = Arrays.copyOf(elements, elements.length, Object[].class);
		for (int i=0; i<elements.length; i++) {
			Object e = elements[i];
			elements[i] = ArrayCollection.empty;
			if (contains(e))
				throw new IllegalArgumentException("multiple "+e+'s');
			elements[i] = e;
		}
	}

	/**@return -1 if o is null or not equal()ed by elements[hash(o)] or a following index with the same hash*/
	@Override protected int indexOf(Object o) {
		if (o == null)
			return -1;
		int hash = hash(o);
		int index = hash;
		do {
			if (elements[index] == null)
				return -1;
			if (elements[index].equals(o))
				return index;
			index++;
		} while (index < elements.length  &&  hash(elements[index]) == hash);
		return -1;
	}

	private static final long serialVersionUID = 1L;
}
