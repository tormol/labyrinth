package tbm.util.collections;

public abstract class UnmodifiableHashSet<E> extends UnmodifiableSet<E> {
	protected UnmodifiableHashSet(Object[] elements) {
		super(elements);
	}
	protected void checkForDuplicates() throws IllegalArgumentException {
		for (int i=0; i<elements.length; i++) {
			Object e = elements[i];
			elements[i] = ArrayCollection.empty;
			if (contains(e))
				throw new IllegalArgumentException("multiple"+e+'s');
			elements[i] = e;
		}
	}
	protected abstract int hash(Object o);
	@Override protected int indexOf(Object o) {
		int index = hash(o);
		if ( !elements[index].equals(o))
			index = -1;
		return index;
	}

	private static final long serialVersionUID = 1L;
}
