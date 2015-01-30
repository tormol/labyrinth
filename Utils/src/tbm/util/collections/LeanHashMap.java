package tbm.util.collections;
import static java.util.Objects.requireNonNull;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public class LeanHashMap<K,V> extends LeanHash<Object> implements Map<K,V>, Iterable<Map.Entry<K,V>> {
	public LeanHashMap(Map<K, V> map) {
		super(requireNonNull(map).size(), 1.2f);
		putAll(map);
	}

	public LeanHashMap(int initialCapacity, float ratio) {
		super(initialCapacity, ratio);
	}

	protected LeanHashMap(Object[] elements, long[] buckets) {
		super(elements, buckets);
	}

	@Override
	protected int ew() {
		return 2;
	}


	public V get(Object key) {
		int index = index(indexOf(key));
		if (index == -1)
			return null;
		return (V)elements[index+1];
	}

	public V put(K key, V value) {
		long hash_index = indexOf(key);
		int index = index(hash_index);
		V prev = null;
		if (index == -1)
			index = add_new(key, hash(hash_index));
		else
			prev = (V)elements[index+1];
		elements[index+1] = value;
		return prev;
	}

	public V remove(Object key) {
		int removed = remove_element(key);
		if (removed == -1)
			return null;
		V old = (V)elements[removed+1];
		elements[removed+1] = null;
		return old;
	}


	/**Uses <tt>o.containsKey()</tt>*/@Override//Object
	public boolean equals(Object obj) {
		if (obj instanceof LeanHashMap)
			return super.equals(obj);
		if (! (obj instanceof Map))
			return false;
		Map<K,V> map = (Map<K,V>)obj;

		if (this.size() != map.size())
			return false;
		//The loop only checks that this is a submap of map, size 

		int i = -ew();
		while ((i = nextAfter(i))  !=  -1);
			if (! map.containsKey(elements[i]))
				return false;
		return true;
	}


	@Override
	public LeanHashMap<K,V> clone() {
		return new LeanHashMap<K,V>(elements.clone(), buckets.clone());
	}


	@Override
	public boolean containsKey(Object key) {
		return index(indexOf(key)) != -1;
	}

	@Override
	public boolean containsValue(Object value) {
		for (int i = 1;  i < elements.length;  i += 2)
			if (elements[i] == value)
				return true;
		return false;
	}
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach((k,v)->put(k,v));
		//// java 7:
		//Iterable<Entry<K,V>> iterable;
		//if (m instanceof Iterable)
		//	iterable = (Iterable<Entry<K,V>>) m;
		//else
		//	iterable = ((Map<K,V>) m).entrySet();
		//for (Entry<K,V> e : iterable)
		//	put(e.getKey(), e.getValue());
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> bc) {
		int i = -2;
		while ((i = nextAfter(i))  !=  -1)
			bc.accept((K)elements[i], (V)elements[i+1]);
	}


	public Collection<V> values() {
		return new ValueCollection();
	}

	protected class ValueCollection extends AbstractCollection<V> {
		@Override public Iterator<V> iterator() {
			return new Iter<V>(-1);//gives offset indexes, => values
		}
		@Override public int size() {
			return size();
		}
	}


	public Set<K> keySet() {
		return new KeySet();
	}

	protected class KeySet extends AbstractSet<K> {
		@Override public Iterator<K> iterator() {
			return new Iter<K>(-2);
		}
		@Override public int size() {
			return size();
		}
	}


	
	/**{@inheritDoc}
	 *Guaranteed fresh Entry instances, also immutable.*/
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	protected class EntrySet extends AbstractSet<Map.Entry<K,V>> {
		@Override public Iterator<Map.Entry<K,V>> iterator() {
			return new Iter<Map.Entry<K, V>>(-2) {
				@Override protected SimpleImmutableEntry<K,V> value(int index) {
					return new SimpleImmutableEntry<K,V>((K)elements[index], (V)elements[index+1]);
				}
			};
		}
		@Override public int size() {
			return size();
		}
	}


	@Override//Iterable
	/**A more memory efficient alternative to entrySet().iterator().
	 *Since this class doesn't use Entries internally, this iterator avoids creating a new Entry for each key/value. 
	 * The Entrys returned by <tt>next()</tt> become invalid after the next next() call.
	 *@returns an iterator that is also an Entry, so <tt>next()</tt> returns itself.*/
	public IterVolatileEntry iterator() {
		return new IterVolatileEntry();
	}

	/**A dangerous but memory efficient Iterator for Entries
	 * If you want to keep the Entry after <tt>next()</tt>, <tt>clone()</tt> will return a SimpleImmutableEntry.*/
	protected class IterVolatileEntry extends Iter<Entry<K,V>> implements Entry<K,V>, Cloneable {
		protected IterVolatileEntry() {
			super(-2);
		}
		@Override//Iter
		protected Entry<K,V> value(int index) {
			return this;
		}

		@Override public K getKey() {
			return (K)elements[lastIndex()];
		}
		@Override public V getValue() {
			return (V)elements[lastIndex()+1];
		}
		@Override public V setValue(V value) {
			int index = lastIndex();
			V old = (V)elements[index+1];
			elements[index+1] = value;
			return old;
		}
		
		@Override public SimpleImmutableEntry<K,V> clone() {
			return new SimpleImmutableEntry<K,V>(this);
		}
		@Override public String toString() {
			return getKey().toString() + '=' + getValue().toString();
		}
	}


	private static final long serialVersionUID = 1L;
}
