package tbm.util.collections;
import java.util.HashSet;
import java.util.Objects;

/**A placeholder until i'm sure IndexedLeanHashSet is bug-free*/  
public class IndexedHashSet<K, E extends HasKey<K>> extends HashSet<E> implements IndexedSet<K, E> {
	/**@{inheritDoc}
	 * Iterates through every element.*/@Override//IndexedSet
	public E get(K key) {
		Objects.requireNonNull(key);
		for (E e : this)
			if (e.getKey().equals(key))
				return e;
		return null;
	}
	private static final long serialVersionUID = 1L;
}