/* Copyright 2016 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util.collections;
import static java.util.Objects.requireNonNull;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class LeanHashMap<K,V> extends LeanHash<Object> implements IterableMap<K,V> {
	protected LeanHashMap(Object[] elements, long[] buckets) {
		super(elements, buckets);
	}

	public LeanHashMap() {
		this(default_initial_size(), default_ratio);
	}
	public LeanHashMap(int initialCapacity, float ratio) {
		super(initialCapacity, ratio);
	}
	public LeanHashMap(Map<K,V> map) {
		this(requireNonNull(map).size(), 1.2f);
		putAll(map);
	}


	@Override
	protected int ew() {
		return 2;
	}


	@Override public void forEach(BiConsumer<? super K, ? super V> bc) {
		for (long bucket : buckets) {
			int start = index(bucket);
			int end = start + elements(bucket)*ew();
			for (int i=start; i<end; i+=ew())
				bc.accept((K)elements[i], (V)elements[i+1]);
		}
	}
	//can't improve on IterableMap's forEach(Entry<>)

	public boolean removeIf(BiPredicate<? super K, ? super V> cond) {
		boolean removed = false;
		for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
			if (cond.test((K)elements[index(b_i)], (V)elements[index(b_i)+1])) {
				remove_index(hash(b_i), index(b_i));
				removed = true;
			}
		return removed;
	}



	@Override public V get(Object key) {
		int index = index(indexOf(key));
		if (index == -1)
			return null;
		return (V)elements[index+1];
	}

	@Override public V put(K key, V value) {
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

	@Override public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
		//// java 7:
		//Iterable<Entry<K,V>> iterable;
		//if (m instanceof Iterable)
		//	iterable = (Iterable<Entry<K,V>>) m;
		//else
		//	iterable = ((Map<K,V>) m).entrySet();
		//for (Entry<K,V> e : iterable)
		//	put(e.getKey(), e.getValue());
	}

	@Override public V remove(Object key) {
		long hash_index = indexOf(key);
		if (index(hash_index) == -1)
			return null;
		V old = (V)elements[index(hash_index)+1];//is cleared by remove_index()
		remove_index(hash(hash_index), index(hash_index));
		return old;
	}

	@Override public boolean remove(Object key, Object value) {
		long hash_index = indexOf(key);
		if (index(hash_index) == -1  ||  !Objects.equals((V)elements[index(hash_index) + 1], value))
			return false;
		elements[index(hash_index) + 1] = null;
		remove_index(hash(hash_index), index(hash_index));
		return true;
	}


	@Override public boolean equals(Object obj) {
		if (obj instanceof LeanHashMap)
			return super.equals(obj);
		if (! (obj instanceof Map))
			return false;
		Map<K,V> map = (Map<K,V>)obj;

		if (this.size() != map.size())
			return false;
		//The loop only checks that this is a submap of map, if there was more elements, sizes would be different

		for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
			if (! map.containsKey(elements[index(b_i)])  ||  !Objects.equals(map.get(elements[index(b_i)]), elements[index(b_i)+1]))
				return false;
		return true;
	}

	@Override public int hashCode() {
		int hashCode = 0;
		for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i)) {
			int keyHash = elements[index(b_i)].hashCode(); //is the same for each key in a bucket.
            //(cannot use bucket index because it's only the n least significant bits.)
			int valHash = elements[index(b_i)+1] == null ? 0 : elements[index(b_i)+1].hashCode();
			hashCode += keyHash ^ valHash;
		}
		return hashCode;
	}

	/**the only difference between map.toString() and map.etrySet().toString() is the enclosing {} or []*/
	protected String toString(char first, char last) {
		StringBuilder sb = new StringBuilder();
		sb.append(first);
		for (int i=0; i<elements.length; i+=ew())//must be the same order as entrySet().iterator()
			if (elements[i] != null)
				sb.append(elements[i]).append('=')
				  .append(elements[i+1]).append(", ");
		if (sb.length() > 1)
			sb.delete(sb.length()-2, sb.length());
		return sb.append(last).toString();
	}

	@Override public String toString() {
		return toString('{', '}');
	}

	@Override public LeanHashMap<K,V> clone() {
		return new LeanHashMap<K,V>(elements.clone(), buckets.clone());
	}



	  //////////
	 //Values//
	//////////

	@Override public boolean containsValue(Object value) {
		for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
			if (Objects.equals(elements[index(b_i) + 1],  value))
				return true;
		return false;
	}

	@Override public ValueCollection values() {
		return new ValueCollection();
	}

	protected class ValueCollection extends AbstractCollection<V> implements CollectionWithToArrayType<V> {
		/**supports <tt>set()</tt>*/
		@Override public ExtendedIterator<V> iterator() {
			return new Iter<V>() {
				@Override public V getIndex(int index) {
					return (V)elements[index + 1];
				}
				@Override public void set(V value) {
					if ( !canRemove)
						throw new IllegalStateException("no element to replace");
					elements[pos + 1] = value;
				}
			};
		}
		@Override public void forEach(Consumer<? super V> action) {
			//LeanHashMap.this.forEach((k,v) -> action.accept(v));
			for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
				action.accept((V) elements[index(b_i) + 1]);
		}
		@Override public int size() {
			return LeanHashMap.this.size();
		}
		@Override public boolean contains(Object value) {
			return LeanHashMap.this.containsValue(value);
		}
		@Override public boolean removeIf(Predicate<? super V> cond) {
			boolean removed = false;
			//LeanHashMap.this.removeIf((k,v)->cond.test(v));
			for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
				if (cond.test((V)elements[index(b_i) + 1 ])) {
					remove_index(hash(b_i),  index(b_i));
					removed = true;
				}
			return removed;
		}
		@Override public boolean removeAll(Collection<?> col) {//removes all occurrences of every element
			return removeIf(col::contains);
		}
		@Override public boolean remove(Object o) {
			//to only return true once, a Predicate varsion would be longer and less readable
			for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
				if (Objects.equals(elements[index(b_i) + 1],  o)) {
					remove_index(hash(b_i),  index(b_i));
					return true;//only remove the first occurrence
				}
			return false;
		}
		@Override public void clear() {
			LeanHashMap.this.clear();
		}
		//toArray and toString might as well be Iterator-based since size is unknown
	}



	  ////////
	 //keys//
	////////

	@Override public boolean containsKey(Object key) {
		if (key == null)
			return false;
		return index(indexOf(key)) != -1;
	}

	@Override public KeySet keySet() {
		return new KeySet();
	}

	protected class KeySet extends AbstractSet<K> implements SetWithGet<K> {
		@Override public ExtendedIterator<K> iterator() {
			return new Iter<K>();
		}
		@Override public int size() {
			return LeanHashMap.this.size();
		}
		@Override public boolean contains(Object key) {
			return LeanHashMap.this.containsKey(key);
		}
		@Override public boolean remove(Object key) {
			boolean removed = contains(key);
			LeanHashMap.this.remove(key);
			return removed;
		}
		@Override public void clear() {
			LeanHashMap.this.clear();
		}
		@Override public K get(Object o) {
			int index = index(indexOf(o));
			return index == -1  ?  null  :  (K)elements[index];
		}
		@Override public int hashCode() {
			return LeanHashMap.super.hashCode();
		}
	}



	  ///////////
	 //entries//
	///////////

	public boolean contains(Object key, Object value) {
		int index = index(indexOf(key));
		return index != -1
			&& Objects.equals(elements[index+1], value);
	}

	public boolean containsAll(Map<?,?> map) {
		for (Entry<?,?> e : IterableMap.iterable(map))
			if ( !contains(e.getKey(), e.getValue()))
				return false;
		return true;
	}

	/**{@inheritDoc}
	 *Guaranteed fresh Entry instances, also immutable.*/
	@Override public EntrySet entrySet() {
		return new EntrySet();
	}

	protected class EntrySet extends AbstractSet<Entry<K,V>> implements CollectionWithToArrayType<Entry<K,V>> {
		@Override public ExtendedIterator<java.util.Map.Entry<K,V>> iterator() {
			return new Iter<Entry<K,V>>() {
				@Override protected MutableEntry getIndex(int index) {
					return new MutableEntry(index);
				}
			};
		}

		protected class MutableEntry extends SimpleEntry<K,V> {
			protected int index;//might change by adding new keys or calling optimize
			//hash becomes invalid if buckets[] is resized, and out of range if it's shrinked
			protected MutableEntry(int index) {
				super((K)elements[index], (V)elements[index+1]);
				this.index = index;
			}
			@Override public V getValue() {
				if (index >= elements.length  ||  index < 0  ||  !getKey().equals(elements[index]))
					index = index(indexOf(getKey()));
				if (index >= 0)
					super.setValue((V)elements[index+1]);
				return super.getValue();
			}
			@Override public V setValue(V new_value) {
				//LeanHashMap.this.replace(getKey(), getValue(), new_value);
				V old_value = getValue();
				if (index >= 0)
					elements[index+1] = new_value;
				return old_value;
			}
			public boolean remove() {
				if (index < 0)
					return false;
				return LeanHashMap.this.remove(getKey(), getValue());
			}
			private static final long serialVersionUID = 1;
		}

		public LeanHashMap<K,V> backingMap() {
			return LeanHashMap.this;
		}
		//methods that don't need a permanent Entry
		@Override public String toString() {
			return LeanHashMap.this.toString('[', ']');
		}
		@Override public int hashCode() {
			return LeanHashMap.this.hashCode();
		}
		@Override public boolean equals(Object o) {
			if (o instanceof LeanHashMap.EntrySet)
				return LeanHashMap.super.equals(((EntrySet)o).backingMap());
			if (o == null  ||  !(o instanceof Set))
				return false;
			Set<?> other = (Set<?>)o;
			return this.size() == other.size()
				&& this.containsAll(other);
		}

		@Override public int size() {
			return LeanHashMap.this.size();
		}
		@Override public boolean contains(Object o) {
			if (o == null  ||  !(o instanceof Entry<?,?>))
				return false;
			Entry<?,?> entry = (Entry<?,?>) o;
			int index = index(indexOf(entry.getKey()));
			return index >= 0  &&  Objects.equals(entry.getValue(), elements[index+1]);
		}
		@Override public boolean containsAll(Collection<?> col) {
			if (col instanceof LeanHashMap.EntrySet)
				return LeanHashMap.this.containsAll( ((EntrySet)col).backingMap() );

			for (Object entry : col)
				if ( !contains(entry))
					return false;
			return true;
		}
		@Override public boolean remove(Object o) {
			if (o == null  ||  !(o instanceof Entry<?,?>))
				return false;
			Entry<?,?> entry = (Entry<?,?>) o;
			return LeanHashMap.this.remove(entry.getKey(), entry.getValue());
		}
		/*Unsupported
		@Override public boolean add(Entry<K,V> entry) {
			boolean change = !LeanHashMap.this.contains(entry.getKey(), entry.getValue());
			put(entry.getKey(), entry.getValue());
			return change;
		}*/
	}



	  ////////////
	 //Iterable//
	////////////

	/**A more memory efficient alternative to entrySet().iterator().
	 *Since this class doesn't use Entries internally, this iterator avoids creating a new Entry for each key/value.
	 * The Entrys returned by <tt>next()</tt> become invalid after the next next() call.
	 *@returns an iterator that is also an Entry, so <tt>next()</tt> returns itself.*/
	@Override public IterVolatileEntry iterator() {
		return new IterVolatileEntry();
	}

	/**A dangerous but memory efficient Iterator for Entries
	 * If you want to keep the Entry after <tt>next()</tt>, <tt>clone()</tt> will return a SimpleImmutableEntry.
	 */
	protected class IterVolatileEntry extends Iter<Entry<K,V>> implements UnsafeEntryIterator<K,V> {
		@Override//Iterable
		protected UnsafeEntryIterator<K,V> getIndex(int index) {
			return this;
		}

		//Iterator
		@Override public UnsafeEntryIterator<K,V> next() {
			return (UnsafeEntryIterator<K,V>) super.next();
		}

		//Entry
		@Override public K getKey() throws IllegalStateException {try {
			return (K)elements[pos];
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalStateException();
		}}
		@Override public V getValue() throws IllegalStateException {try {
			return (V)elements[pos + 1];
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalStateException();
		}}
		@Override//Entry, not ListIterator
		public V setValue(V value) throws IllegalStateException {try {
			V old = (V)elements[pos+1];
			elements[pos+1] = value;
			return old;
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalStateException();
		}}

		//Entry
		@Override public String toString() {
			return UnsafeEntryIterator.toString_of(this);
		}
		@Override public int hashCode() {
			return UnsafeEntryIterator.hashCode_of(this);
		}
		@Override public boolean equals(Object o) {
			return UnsafeEntryIterator.equals_of(this, o);
		}
	}


	private static final long serialVersionUID = 1L;
}
