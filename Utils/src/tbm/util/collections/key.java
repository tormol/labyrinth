package tbm.util.collections;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import tbm.util.collections.LeanHashSet;

public final class key {
	public static interface KeyObject<K> {
		K getKey();
	}

	public static abstract class AbstractKeyObject<K> implements KeyObject<K> {
		protected final K key;
		protected AbstractKeyObject(K key) {
			this.key = Objects.requireNonNull(key);
		}
		@Override//KeyObject
		public final K getKey() {
			return key;
		}
		/**
		 *@return key.hashCode()*/@Override//Object
		public final int hashCode() {
			return key.hashCode();
		}
		@Override//Object
		public abstract boolean equals(Object o);
		@Override//Object
		public String toString() {
			return key.toString();
		}
	}

	public interface KeySet<E extends KeyObject<K>, K> extends Set<E> {
		E get(K key);
	}

	public static class IndexedHashSet<E extends KeyObject<K>, K> extends LeanHashSet<E> implements KeySet<E,K> {
		public IndexedHashSet(int initialCapacity, float load_factor) {
			super(initialCapacity, load_factor);
		}
		public IndexedHashSet(Collection<E> col) {
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
		public IndexedHashSet<E,K> clone() {
			return (IndexedHashSet<E,K>)super.clone();
		}
		private static final long serialVersionUID = 1L;
	}
}
