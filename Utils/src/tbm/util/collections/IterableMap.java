package tbm.util.collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**A map that implements Iterable directly, and which returned Entry might be reused for every key/value pair
 * The default implementation does that to prove the point, so
 * maps using Entry internally should override to {@code return entrySet().iterator();}*/
public interface IterableMap<K,V> extends Map<K,V>, Iterable<Entry<K,V>> {
	/**the returned entries should not be used outside the loop*/
	//Iterator<Entry<>> instead of UnsafeEntryIterator<> to allow HashMap to return entrySet().iterator()
	@Override default Iterator<Entry<K, V>> iterator() {
		return new ImplUnsafeEntryIterator<K,V>(this);
	}

	@Override default void forEach(BiConsumer<? super K, ? super V> action) {
		for (Entry<K,V> entry : this)
			action.accept(entry.getKey(), entry.getValue());
	}


	/**return the potentially unsafe {@code map.iterator()} if aviable, else return {@code map.entryset().iterator()}*/
	public static <K,V> Iterable<? extends Entry<K,V>> iterable(Map<K,V> map) {
		if (map instanceof IterableMap<?,?>)
			return (IterableMap<K,V>)map;
		return map.entrySet();
	}



	/**Can be used as a memory-efficient Iterator for maps that don't use Entry objects internally.
	 * by being both an iterator and an entry, the result of getKey() change after each next() */
	public interface UnsafeEntryIterator<K,V> extends Iterator<Entry<K,V>>, Entry<K,V> {
		/**{@inheritDoc}
		 *@return {@code this}*/
		@Override UnsafeEntryIterator<K,V> next();

		@Override void remove();//should support

		/**get an immutable copy of the current entry*/
		default SimpleImmutableEntry<K,V> immutableCopy() {
			return new SimpleImmutableEntry<K,V>(this);
		}


		//static Entry utility methods
		public static String toString_of(Entry<?,?> entry) {
			return entry.getKey().toString()+'='+entry.getValue();
		}
		public static int hashCode_of(Entry<?,?> entry) {
			return entry.getKey().hashCode()
				^  entry.getValue().hashCode();
		}
		public static boolean equals_of(Entry<?,?> self, Object o) {
			return o != null
				&& o instanceof Entry<?,?>
				&& self.getKey().equals(((Entry<?,?>)o).getKey())
				&& Objects.equals(self.getValue(), ((Entry<?,?>)o).getValue());
		}
	}



	/**An implementation of UnsafeEntryIterator.
	 * Uses keySet().iterator() to avoid being safe.*/
	public static class ImplUnsafeEntryIterator<K,V> implements UnsafeEntryIterator<K,V> {
		protected final Map<K,V> map;
		protected final Iterator<K> keys;
		protected K key;
		protected V value;
		public ImplUnsafeEntryIterator(Map<K,V> map) {
			this.map = map;
			keys = map.keySet().iterator();
		}

		@Override public boolean hasNext() {
			return keys.hasNext();
		}
		@Override public ImplUnsafeEntryIterator<K,V> next() {
			key = keys.next();
			value = map.get(key);
			return this;
		}
		@Override public void remove() {
			keys.remove();
		}

		@Override public K   getKey() {return key;}
		@Override public V getValue() {return value;}
		@Override public V setValue(V value) {
			V old_value = value;
			if (map.containsKey(key))
				map.put(key, value);
			this.value = value;
			return old_value;
		}
	
		@Override public  String toString()       {return UnsafeEntryIterator.toString_of(this);}
		@Override public     int hashCode()       {return UnsafeEntryIterator.hashCode_of(this);}
		@Override public boolean equals(Object o) {return UnsafeEntryIterator.equals_of(this, o);}
	}
}
