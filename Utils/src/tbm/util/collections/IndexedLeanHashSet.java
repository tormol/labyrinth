package tbm.util.collections;

import java.util.Collection;

public class IndexedLeanHashSet<K, E extends HasKey<K>> extends LeanHashSet<E> implements IndexedSet<K,E> {
	public IndexedLeanHashSet(int initialCapacity, float load_factor) {
		super(initialCapacity, load_factor);
	}
	public IndexedLeanHashSet(Collection<E> col) {
		super(col);
	}
	@Override
	protected boolean compare(int index, Object o) {
		return elements[index].equals(o)
		    || elements[index].getKey().equals(o);
	}
	@Override
	public E get(K key) {
		int index = index(indexOf(key));
		return index==-1 ? null : elements[index];
	}
	@Override @SuppressWarnings("unchecked")
	public IndexedLeanHashSet<K,E> clone() {
		return (IndexedLeanHashSet<K,E>)super.clone();
	}
	private static final long serialVersionUID = 1L;
}