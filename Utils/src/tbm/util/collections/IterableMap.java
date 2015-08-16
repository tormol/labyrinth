package tbm.util.collections;
import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

/**A map that implements Iterable directly, and which returned Entry might be reused for every key/value pair
 * The default implementation does that to prove the point, so
 * maps using Entry internally should override to {@code return entrySet().iterator();}*/
public interface IterableMap<K,V> extends Map<K,V>, Iterable<Entry<K,V>> {
	/**the returned Entryes should not used outside the loop*/
	@Override default Iterator<Entry<K, V>> iterator() {
		return new ImplUnsafeMapIterator<K,V>(this);
	}



	/**Can be used
	 * @param <K> key
	 * @param <V> value
	 * @param <M> map
	 */
	public interface UnsafeMapIterator<K,V, M extends Map<K,V>> extends Iterator<Entry<K,V>>, Entry<K,V> {
		M forMap();
		/**{@inheritDoc}
		 *@return {@code this}*/
		@Override UnsafeMapIterator<K,V, M> next();
		@Override default V getValue() {
			return forMap().get(getKey());
		}
		@Override default V setValue(V value) {
			return forMap().put(getKey(), getValue());
		}
		@Override default void remove() {
			forMap().remove(getKey());
		}
		/**get an immutable copy of the current entry*/
		default SimpleImmutableEntry<K,V> immutableCopy() {
			return new SimpleImmutableEntry<K,V>(this);
		}
	}



	/**An implementation of UnsafeMapIterator*/
	public static class ImplUnsafeMapIterator<K,V> implements UnsafeMapIterator<K, V, Map<K,V>> {
		protected final Map<K,V> map;
		protected final Iterator<K> keys;
		protected K key;
		protected V value;
		public ImplUnsafeMapIterator(Map<K,V> map) {
			this.map = map;
			keys = map.keySet().iterator();
		}
		@Override public boolean hasNext() {
			return keys.hasNext();
		}
		@Override public ImplUnsafeMapIterator<K,V> next() {
			key = keys.next();
			value = map.get(key);
			return this;
		}
		@Override public K getKey() {return key;}
		@Override public V getValue() {return value;}
		@Override public Map<K,V> forMap() {return map;}

		@Override public String toString() {
			return getKey().toString() + '=' + getValue().toString();
		}
		@Override public SimpleImmutableEntry<K,V> clone() {
			return immutableCopy();
		}
	}
}
