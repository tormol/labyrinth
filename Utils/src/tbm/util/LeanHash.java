package tbm.util;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import tbm.defaultCollections.IAbstractMap;
import tbm.defaultCollections.IAbstractSet;

@SuppressWarnings("unchecked")
public abstract class LeanHash<E> implements Cloneable, Serializable {//Cannot implement Collection as Map doesn't.
	protected E[] elements;
	protected long[] buckets;//struct{int entries, first_index}
	protected LeanHash(LeanHash<E> lh) {
		Objects.requireNonNull(lh);
		buckets = Arrays.copyOf(lh.buckets, lh.buckets.length);
		elements = Arrays.copyOf(lh.elements, lh.elements.length);
	}
	protected LeanHash(int initialCapacity, float load_factor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("initialCapacity is less than zero, but is "+initialCapacity+".");
		if (load_factor <= 0  ||  1 < load_factor)
			throw new IllegalArgumentException(
					 "load factor must be greater than zero and "
					+"less than or equal to one, but is "+load_factor+".");
		elements = (E[])new Object[(int)(initialCapacity / load_factor) +1];
		buckets = new long[initialCapacity];
	}


	final protected int elements(long struct) {
		return (int)(struct >> 32);
	}
	final protected int index(long struct) {
		return (int)(struct & 0xffffffff);
	}
	public final int hash(Object value) {
		return value.hashCode() & (buckets.length-1);
	}
	final protected long new_bucket(int entries, int first_index) {
		return entries << 32  |  first_index;
	}
	protected boolean compare(int index, Object o) {
		return elements[index].equals(o);
	}
	/**@returns struct{int hash, index}*/
	protected long indexOf(Object element_or_key) {
		int hash = hash(element_or_key);
		int from = index(buckets[hash]);
		int to = from + elements(buckets[hash]);
		for (int i = from;  i < to;  i++)
			if (elements[i].equals(element_or_key))
				return (long)(hash)<<32 | i; 
		return (long)(hash) | 0xffffffff;
	}

	/**make room for another entry with the same key.
	 *@return the index to put the new object, or -1 if no space found*/
	final protected int fit(int hash)  {
		long bucket = buckets[hash];
		int before = index(bucket)-1;
		int after = index(bucket) + elements(bucket) + 1;
		if (before >= 0  &&  elements[index(bucket)-1] == null) {
			buckets[hash] += (1<<33)-1;//increment entries and decrement index
			return before;
		} else if (after < elements.length  &&  elements[after] == null) {
			buckets[hash] += 1<<32;
			return after;
		} else if (after < elements.length  &&  elements(buckets[hash(elements[after])]) == 1) {
			if (move_one(after, hash, 1<<32))
				return after;
		} else if (before >= 0  &&  elements(buckets[hash(elements[before])]) == 1) {
			if (move_one(before, hash, (1<<33)-1))
				return before;
		} else {//move this
			int found = 0;
			for (int i = elements.length-1;  i>=0;  i--)
				if (elements[i] == null)
					if (found == elements(bucket)) {
						System.arraycopy(elements, index(bucket), elements, i, elements(bucket));
						buckets[hash] = (elements(bucket)+1)<<32 | i; 
						Arrays.fill(elements, before+1, after-1, null);
						return i;
					} else
						found++;
				else
					found = 0;
		}
		return -1;
	}
	private boolean move_one(int to_move, int bucket_index, int add_to_bucket) {
		for (int i=elements.length-1;  i>=0;  i--)
			if (elements[i] == null) {
				elements[i] = elements[to_move];
				buckets[hash(elements[to_move])] = 1<<32 | i;
				buckets[bucket_index] += add_to_bucket;
				return true;
			}
		return false;
	}
	protected void grow_and_rehash() {
		E[] old_elements = elements;
		elements = (E[])new Object[elements.length * 2];
		long[] old_buckets = buckets;
		buckets = new long[buckets.length * 2];//this way i can use hash()
		int next = 0;
		for (int i=0; i<old_buckets.length; i++)
			if (elements(old_buckets[i]) == 1) {
				elements[next] = old_elements[index(old_buckets[i])];
				buckets[hash(elements[next])] = new_bucket(1, next);
				next++;
			} else if (elements(old_buckets[i]) != 0) {
				//an empty slot between them
				int a_first = next,  b_last = next + elements(old_buckets[i]);
				int a_count = 0,  b_count = 0;
				next = b_last + 1;
				
				for (int ii=index(old_buckets[i]); ii>=0;  ii--)
					if (hash(old_elements[ii]) == i) {//a
						elements[a_first + a_count] = old_elements[ii];
						a_count++;
					} else {//b
						elements[b_last - b_count] = old_elements[ii];
						b_count++;
					}
				if (a_count > 0)
					buckets[i] = new_bucket(a_count, a_first);
				if (b_count > 0)
					buckets[i + buckets.length/2] = new_bucket(b_count, b_last-b_count+1);
			}
	}

	/**Copy elements from one index to another.
	 * If from==-1 copy a null.*/
	protected abstract void copy_elements(int from, int to);
	protected void remove(long hash_index) {
		int hash = (int)(hash_index>>32);
		E set_to = null;
		long bucket;
		if (elements(buckets[hash]) == 1) {//only element in bucket, remove bucket
			bucket = 0;
		} else if (index(hash_index) == index(buckets[hash])) {//first element, increment index in bucket
			bucket = buckets[hash] - (1<<33)-1;//decrement elements, increment index
		} else {
			bucket = buckets[hash] - 1<<32;
			if (index(hash_index) != index(buckets[hash]) + elements(buckets[hash]) - 1) {//last element, don't have to move
				set_to = elements[index(bucket) + elements(bucket)];
				elements[index(bucket) + elements(bucket)] = null;
			}
		}
		elements[index(hash_index)] = set_to;
		buckets[hash] = bucket;
	}

	protected int _add(E element) {
		long hash_index = indexOf(element);
		int index = index(hash_index);
		int hash = (int)(hash_index>>32);
		if (index != -1) {
			//replace old value
			elements[index] = element;
		} else if (elements(buckets[hash]) == 0) {
			//new bucket
			int i = elements.length-1;
			for (;  i != -1;  i--)
				if (elements[i] == null)
					break;
			if (i == -1) {
				grow_and_rehash();
				i = elements.length-1;
			}
			elements[i] = element;
			buckets[hash] = new_bucket(1, i);
		} else
			//add to existing bucket
			while (true) {
				index = fit(hash);
				if (index == -1)
					grow_and_rehash();
				else
					elements[index] = element;
			}
		return index;
	}

	//@Override
	protected E _get(Object element_or_key) {
		int index = index(indexOf(element_or_key));
		return index==-1 ? null : elements[index];
	}

	protected int remove(Object o) {
		long hash_index = indexOf(o);
		if (index(hash_index) == -1)
			return false;
		remove(hash_index);
		return false;
	}

	protected Iterator<E> iter_elements() {
		return new Iter();
	}

	protected int nextAfter(int index) {
		do {
			index++;
			if (index == el.length)
				return -1;
		} while (elements[index] == null);
		return index;
	}




	public void clear() {
		Arrays.fill(elements, null);
		Arrays.fill(buckets, 0);
	}
	
	public boolean contains(Object o) {
		return index(indexOf(o)) != -1;
	}

	public boolean isEmpty() {
		return size()==0;
	}

	/**Compute number of elements, is not instant.*/
	public int size() {
		int size=0;
		for (long bucket : buckets)
			size+= elements(bucket);
		return size;
	}

	/**Is slow*/@Override
	public int hashCode() {
		return size();
	}

	/**Is slow*/@Override
	public boolean equals(Object o) {
		if  (o == null  ||  o.getClass() != this.getClass())
			return false;
		Iterator<E> a = iter_elements();
		Iterator<E> b = ((LeanHash<E>)o).iter_elements();
		while (true) {
			if (!a.hasNext())
				return !b.hasNext();
			if (!a.next().equals(b.next()))
				return false;
		}
	}
	private static final long serialVersionUID = 1L;



	protected abstract class Iter<T> implements Iterator<T> {
		private int prev=-1, next=-1;
		private final E[] el = elements;
		@Override
		public boolean hasNext() {
			if (el != elements)
				throw new ConcurrentModificationException();
			if (next == prev) {
				next = nextAfter(next);
				if (next == -1)
					return false;
			}
			return true;
		}

		abstract protected T value(int index);
		@Override
		public T next() {
			if (!hasNext())
				return null;
			prev = next;
			return value(next);
		}

		@Override
		public void remove() throws RuntimeException {
			hasNext();//check ConcurrentModificationException and advance next since (next != prev) becomes false;  
			if (prev == -1)
				throw new RuntimeException("No element to remove.");
			LeanHash.this.remove((long)hash(el[prev])<<32 | prev);
			prev = -1;
		}
	}

	protected final class IterElements extends Iter<E> {
		@Override protected E value(int index) {
			return elements[index];
		}
	}



	public static class LeanHashSet<E> extends LeanHash<E> implements IAbstractSet<E> {
		protected LeanHashSet(int initialCapacity, float load_factor) {
			super(initialCapacity, load_factor);
		}
		public LeanHashSet(LeanHash<E> hs) {
			super(hs);
		}

		@Override
		public boolean add(E e) {
			_add(e);
			return true;
		}

		protected final void copy_elements(int from, int to) {
			elements[to] = from==-1 ? null : elements[from];
		}
		@Override
		public Object[] toArray() {
			Object[] copy = new Object[size()];
			int put = 0;
			for (int i = elements.length-1;  i >= 0;  i--)
				if (elements[i] != null) {
					copy[put] = elements[i];
					put++;
				}
			return copy;
		}

		public <T> T[] toArray(T[] copy) {
			if (copy == null)
				return (T[]) toArray();
			int put=copy.length, from = elements.length;
			while (from > 0) {
				from--;
				if (elements[from] != null) {
					if (put == 0) {
						put = 1;
						for (int i=0;  i<from;  i++)
							if (elements[from] != null)
								put++;
						T[] new_copy = (T[])new Object[copy.length+put];
						System.arraycopy(copy, 0, new_copy, put, copy.length);
						copy = new_copy;
					}
					put--;
					copy[put] = (T)elements[from];
				}
			}
			return copy;
		}

		public Iterator<E> iterator() {
			return new Iter();
		}
		@Override
		public LeanHashSet<E> clone() {
			return new LeanHashSet<E>(this);
		}
		private static final long serialVersionUID = 1L;
	}



	public static interface Indexed {
		boolean isIndexOrEqual(Object o);
	}
	public static class IndexedHashSet<E extends Indexed> extends LeanHashSet<E> {
		protected IndexedHashSet(int initialCapacity, float load_factor) {
			super(initialCapacity, load_factor);
		}
		public IndexedHashSet(LeanHash<E> hs) {
			super(hs);
		}
		@Override
		protected boolean compare(int index, Object o) {
			return elements[index].isIndexOrEqual(o);
		}
		@Override
		public IndexedHashSet<E> clone() {
			return new IndexedHashSet<E>(this);
		}
		private static final long serialVersionUID = 1L;
	}



	public static class LeanHashMap<K, V> extends LeanHash<K> implements IAbstractMap<K, V> {
		protected V[] values;
		public LeanHashMap(LeanHashMap<K, V> map) {
			super(map);
			values = Arrays.copyOf(map.values, map.values.length);
		}
		protected LeanHashMap(int initialCapacity, float load_factor) {
			super(initialCapacity, load_factor);
			// TODO Auto-generated constructor stub
		}
		protected final void copy_elements(final int from, final int to) {
			elements[to] = from==-1 ? null : elements[from];
			values[to] = from==-1 ? null : values[from];
		}
		@Override
		public Set<java.util.Map.Entry<K, V>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public V get(Object key) {
			int index = index(indexOf(key));
			if (index == -1)
				return null;
			return values[index];
		}
		@Override
		public Set<K> keySet() {
			return new KeySet();
		}
		@Override
		public V put(K key, V value) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public V remove(Object key) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Collection<V> values() {
			// TODO Auto-generated method stub
			return null;
		}

		class KeySet extends AbstractSet<K> {
			@Override
			public Iterator<K> iterator() {
				return new IterElements();
			}
			@Override
			public int size() {
				return LeanHashMap.this.size();
			}
		}
		protected final class IterValues extends Iter<V> {
			@Override protected V value(int index) {
				return values[index];
			}
		}
		protected final class IterElements extends Iter<Entry<K, V>> {
			@Override protected Entry<K,V> value(int index) {
				return new <K,V>(elements[index], values[index]);
			}
		}
		private static final long serialVersionUID = 1L;
	}
}
