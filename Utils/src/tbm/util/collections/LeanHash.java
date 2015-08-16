package tbm.util.collections;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;

/**
 *A base class to create memory efficient hash structures
 *Uses one array with elements and another to store hash information.
 *Since multiple entries can have the same hash, A separate buckets array is 
 *used as lookup from a hash value, and store the first index to elements with
 *that hash, the number of those elements.
 *To avoid creating an object for each bucket, the two integers are stored as
 *one long, and extracted by bitwise operations
 *
 *Doesn't implement Collection since Map doesn't.
 **/@SuppressWarnings("unchecked")
abstract class LeanHash<E> implements Cloneable, Serializable {
	/**struct{int entries, first_index}
	 * the 32 lsbs store the first index of elements with this hash
	 * and the otheer half store the number of elements with this hash*/
	protected long[] buckets;//struct{int entries, first_index}
	protected E[] elements;

	public static final int default_initial_buckets = 8;
	public static final int minimum_initial_buckets = 4;
	/**elements / buckets*/
	public static final float default_ratio = 1.25f;
	/**@return (int)(default_initial_buckets * default_ratio)*/
	public static final int default_initial_size() {
		return Math.round(default_initial_buckets * default_ratio);
	}

	/**@return this(default_initial_buckets, default_ratio)*/
	public LeanHash() {
		this(default_initial_buckets, default_ratio);
	}
	/**
	 *@param initial_buckets number of buckets, will be rounded up to a power of two. Cannot be negative
	 *@param ratio elements / buckets.
	 * Not analogous to load_factor as it doesn't directly affect resizing,
	 * but higher value will lead to less resizing and worse performance.
	 * Must be positive.
	 * */
	public LeanHash(int initial_buckets, float ratio) {
		if (! (ratio > 0))//ratio <= 0 won't catch NaN
			throw new IllegalArgumentException( "ratio must be positive, but is "+ratio+".");
		else if (initial_buckets < minimum_initial_buckets)
			if (initial_buckets == 0)//Tests require this be valid
				initial_buckets = default_initial_buckets;
			else if (initial_buckets < 0)
				throw new IllegalArgumentException("initial_buckets must be positive, but is "+initial_buckets+".");
			else
				initial_buckets = minimum_initial_buckets;
		else if ((initial_buckets & (initial_buckets-1)) != 0)//not a power of two
				initial_buckets = Integer.highestOneBit(initial_buckets) << 1;
		buckets = new long[initial_buckets];
		elements = (E[])new Object[ Math.round(initial_buckets * ratio)*ew() ];
	}

	/**Create an instance with these variables, does not copy, nor check for nulls*/
	protected LeanHash(E[] elements, long[] buckets) {
		this.elements = elements;
		this.buckets = buckets;
	}


	/**element width or index factor.
	 *Is one for sets, and two for maps, so they can be stored key,value,key,value*/
	//FIXME: can this be inlined?
	protected abstract int ew();


	/***/
	protected final long new_bucket(int entries, int first_index) {
		if (entries < 1  ||  first_index < 0)
			throw new RuntimeException("Invalid input, entries = "+entries+", first_index = "+first_index);
		return (long)entries << 32  |  first_index;
	}
	/**Get the number of elements in this bucket.
	 *@return bucket>>32*/
	protected final int elements(long bucket) {
		return (int)(bucket >> 32);
	}
	/**Get the elements[] index of the first element out of a long bucket.
	 *@return bucket & 0xffff ffff*/
	protected final int index(long bucket) {
		return (int)(bucket & 0xffffffffL);
	}
	protected final int hash(long hash_index) {
		return (int)(hash_index >> 32);
	}
	/**@return value.hashCode() & (buckets.length-1)*/
	public int hash(Object value) {
		return value.hashCode() & (buckets.length-1);
	}


	/**@return the first half is hash and the second half is index or -1
	 * @throws NullPointerException if parameter is <tt>null</tt>*/
	protected final long indexOf(Object element_or_key) {
		requireNonNull(element_or_key);
		int hash = hash(element_or_key);
		int from = index(buckets[hash]);
		int to = from + elements(buckets[hash]) * ew();
		for (int i = from;  i < to;  i += ew())
			if (elements[i].equals(element_or_key))
				return (long)(hash)<<32 | i;
		return (long)(hash)<<32 | 0xffffffffL;
	}


	/**Add a new element
	 *@param element must not be equal to another element, or null
	 *@param hash {@code hash(element)}
	 *@return index of element*/
	protected int add_new(E element, int hash) {
		if (element == this)
			throw new RuntimeException("Cannot add itself.");//FIXME: exception type
		int empty;
		if (elements(buckets[hash]) == 0) {
			//new bucket
			empty = elements.length-ew();//grow_and_rehash() moves elements toward the start, so searching from the end should give a result quicker.
			for (;  empty >= 0;  empty -= ew())
				if (elements[empty] == null)
					break;
			if (empty < 0)
				empty = grow_and_rehash(true);
			buckets[hash] = new_bucket(1, empty);
		} else
			empty = fit(hash);
		elements[empty] = element;
		return empty;
	}

	/**make room for another entry with the same hash
	 *First checks if index before or after are free,
	 *Then checks if before and after are their own bucket and try to move that.
	 *At last tries to move all elements in this bucket.
	 *<p>
	 *Calls <tt>move_many()</tt> and <tt>copy_elements()</tt>
	 *so that LeanHashMap doesn't have to duplicate this method.
	 *@return the index to put the new object, or -1 if no space was found.*/
	protected int fit(int hash)  {
		long bucket = buckets[hash];
		int before = index(bucket) - ew();
		int after = index(bucket) + elements(bucket) * ew();

		//null before or after
		if (before >= 0  &&  elements[before] == null) {
			buckets[hash] += (1L<<33)-ew();//increment entries and decrement index
			return before;
		} else if (after < elements.length  &&  elements[after] == null) {
			buckets[hash] += 1L<<32;
			return after;
		}

		//the element before or after is alone in its bucket, so move that
		else if (after < elements.length  &&  elements(buckets[hash(elements[after])]) == 1) {
			if (relocate_single(after, hash, 1L<<32))
				return after;
		} else if (before >= 0  &&  elements(buckets[hash(elements[before])]) == 1) {
			if (relocate_single(before, hash, (1L<<33)-ew()))
				return before;
		} else {//move this
			int found = 0;
			for (int i = elements.length-ew();  i >= 0;  i -= ew())
				if (elements[i] == null)
					if (found == elements(bucket)) {
						buckets[hash] = new_bucket(elements(bucket), i);
						System.arraycopy(elements, index(bucket), elements, i, elements(bucket)*ew());
						Arrays.fill(elements, index(bucket), index(bucket)+elements(bucket)*ew(), null);
						return i;
					} else
						found++;
				else
					found = 0;
		}

		grow_and_rehash(true);
		int index = fit(hash);
		if (index == -1) {
			pack(elements);
			index = elements.length-ew();
		}
		return index;
	}

	/**Find a single unused slot and move an element to it.
	 *Code was used twice in <tt>fit()</tt>*/
	private boolean relocate_single(int to_move, int bucket_index, long add_to_bucket) {
		for (int i=elements.length-ew();  i>=0;  i-=ew())
			if (elements[i] == null) {
				System.arraycopy(elements, to_move, elements, i, ew());
				buckets[hash(elements[to_move])] = 1<<32 | i;
				buckets[bucket_index] += add_to_bucket;
				return true;
			}
		return false;
	}


	
	/**Multiply the size of arrays with grow_ratio,
	 * and rehash since all buckets are split in two.
	 *@param double_and_spread double size of elements along with buckets, and try spread out the elements. 
	 *@return a free slot if <tt>double_and_spread</tt> or <tt>elements.length</tt.*/
	protected int grow_and_rehash(boolean double_and_spread) {
		E[] old_elements = elements;
		if (double_and_spread) {
			elements = (E[])new Object[ elements.length * 2 ];
			//for each old_bucket there might be added an empty slot
			if (elements.length/ew() <= 1+buckets.length)
				double_and_spread = false;
		} else
			elements = (E[])new Object[ elements.length ];//when splitting buckets elements will be moved around without a buffer
		double_and_spread = false;//debug
		
		long[] old_buckets = buckets;
		buckets = new long[buckets.length * 2];//this way hash() works on the new array.
		int next = 0;

		//obi = old_buckets index
		for (int obi = 0;  obi < old_buckets.length;  obi++) {
			if (elements(old_buckets[obi]) == 1) {
				System.arraycopy(old_elements, index(old_buckets[obi]), elements, next, ew());
				buckets[hash(elements[next])] = new_bucket(1, next);
				next += ew();
			} else if (elements(old_buckets[obi]) > 0) {
				//bucket is split in two, because one more bit from the hash is used, a is for bit 0,
				//puts an empty slot between them by not subtracting ew() from b_last
				int a_count = 0,  a_first = next;
				int b_count = 0,  b_last  = next + elements(old_buckets[obi])*ew() - ew();
				next = b_last + ew();

				int oes = index(old_buckets[obi]);//old_elements start
				//oei = old_elements index
				for (int oei = oes;  oei < oes - elements(old_buckets[obi]);  oei -= ew())
					if (hash(old_elements[oei]) == obi) {//a; new bit is 0
						System.arraycopy(old_elements, oei, elements, a_first+a_count, ew());
						a_count += ew();
					} else {//b; new bit is 1
						System.arraycopy(old_elements, oei, elements, b_last-b_count, ew());
						b_count += ew();
					}
				if (a_count > 0)
					buckets[obi] = new_bucket(a_count, a_first);
				if (b_count > 0)
					buckets[obi + buckets.length/2] = new_bucket(b_count, b_last-b_count+1);
			}
			if (double_and_spread)
				next += ew();//skip one slot
		}
		return next;
	}


	public float get_ratio() {
		return (float)(elements.length / ew())  /  buckets.length;
	}

	public boolean set_ratio(float ratio) {
		if (ratio <= 0)
			throw new IllegalArgumentException("negative ratio: "+ratio);
		if (Math.abs(ratio - get_ratio()) < 0.05)
			return true;
		if (ratio > get_ratio()) {
			Object[] new_elements = new Object[(int)(buckets.length*ratio)];
			System.arraycopy(elements, 0, new_elements, 0, elements.length);
			elements = (E[])new_elements;
			return true;
		}
		int size = size();
		if (buckets.length*ratio < size)
			return false;
		E[] new_elements = (E[])new Object[size];
		pack(new_elements);
		elements = new_elements;
		return true;
	}

	public int capacity() {
		return elements.length;
	}


	/**Move all elements to the start of the array, passing <tt>this.elements</tt> is safe.*/
	private int pack(Object[] new_elements) {
		int to = 0;
		//copy everything until the first null
		while (to < elements.length  &&  elements[to] != null)
			to += ew();
		System.arraycopy(elements, 0, new_elements, 0, to);
		//copy everything after
		int from = to + ew();
		while (from < elements.length)
			if (elements[from] == null)
				from += ew();
			else {
				int hash = hash(elements[from]);
				int entries = elements(buckets[hash]);
				buckets[hash] = new_bucket(entries, to);
				entries *= ew();
				int distance = from - to;
				if (distance < entries)//old and new positions are overlapping, only move some
					System.arraycopy(elements, from+entries-distance, new_elements, to, distance);
				else
					System.arraycopy(elements, from, new_elements, to, entries);
				from += entries;
				/*Optimized:
				 *int distance = Math.min(from-to, entries);
				 *from += entries;
				 *System.arraycopy(elements, from-distance, new_elements, to, distance);
				 */
				to += entries;
			}
		//zero out everything after
		Arrays.fill(new_elements, to, new_elements.length, null);
		return to / ew();
	}

	/***/
	public static final float resize_threshold = 0.85f;

	/**Untested
	 * Reduce capacity if sensible.
	 *@return whether anything was done, currently false*/
	public boolean optimize() {
		//get number of hashes and elements
		int size=0, hashes=0;
		for (long bucket : buckets)
			if (elements(bucket) > 0) {
				size += elements(bucket);
				hashes++;
			}
		boolean did_anything = false;
		if (hashes*2 <= buckets.length) {
			//did_anything = true;
			//shrink buckets[] and rehash 
		}
		if (size > hashes*0.75f)
			grow_and_rehash(false);//and then size < elements.length*0.85f, inefficient, but simple. 
		if (size < elements.length*0.85f) {
			did_anything = true;
			E[] new_elements = (E[])new Object[size];
			pack(new_elements);
			elements = new_elements;
		}
		return did_anything;
	}/**Untested*/
	public boolean optimize2() {
		//get number of hashes and elements
		int size=0, hashes=0;
		for (long bucket : buckets)
			if (elements(bucket) > 0) {
				hashes++;
				size += elements(bucket);
			}
		E[] new_elements = elements;
		if (size < elements.length*0.85f)
			new_elements = (E[])new Object[size];
		if (buckets.length >= 2*hashes) {
			long[] new_buckets = buckets;
			if (Integer.highestOneBit(hashes) != hashes)
				hashes = 2 * Integer.highestOneBit(hashes);
			int next = 0;
			new_buckets = new long[hashes];
			for (int nb=0; nb<hashes; nb++) {
				int entries = 0;
				for (int b=nb; b<buckets.length; b += hashes)
					if (elements(buckets[b]) > 0) {
						System.arraycopy(elements, index(buckets[b]),
						             new_elements, next+entries, ew()*elements(buckets[b]));
						entries += ew()*elements(buckets[b]);
					}
				if (entries > 0)
					new_buckets[nb] = new_bucket(entries/ew(), next);
				next += entries;
			}
			buckets = new_buckets;
		} else if (size > hashes*0.75f)
			grow_and_rehash(false);
		else if (size < elements.length*0.85f)
			pack(new_elements);
		else
			return false;
		elements = new_elements;
		return true;
	}


	//Used by Iter.remove and remove_element 
	protected void remove_index(int hash, int to_remove) {
		if (elements(buckets[hash]) == 1)//only element in bucket, remove bucket
			buckets[hash] = 0;
		else if (to_remove == index(buckets[hash]))//first element, increment index in bucket
			buckets[hash] -= (1L<<33)-ew();//decrement elements, increment index
		else {
			buckets[hash] -= 1L<<32;//decrement elements, index+elements now point at the last element
			int last_element = index(buckets[hash]) + ew()*elements(buckets[hash]) - ew();
			if (to_remove != last_element) {
				System.arraycopy(elements, last_element, elements, to_remove, ew());
				to_remove = last_element;
			}
		}
		Arrays.fill(elements, to_remove, to_remove+ew(), null);
	}

	protected int remove_element(Object o) {
		long hash_index = indexOf(o);
		if (index(hash_index) != -1)
			remove_index(hash(hash_index), index(hash_index));
		return index(hash_index);
	}

	/**Get the next non-null element after the given index, or -1
	 * final because null is
	 *@return the index of the n*/
	protected final int nextAfter(int index) {
		do {
			index += ew();
			if (index >= elements.length)
				return -1;
		} while (elements[index] == null);
		return index;
	}

	/**Removes all elements, but does not reduce the capacity*/
	public void clear() {
		Arrays.fill(elements, null);
		Arrays.fill(buckets, 0);
	}

	/**Removes all elements reduce to 8 buckets and keep the ratio*/
	public void clear_and_shrink() {
		if (buckets.length > 8) {
			float elsize = 8 * elements.length / (float)buckets.length;
			elements = (E[]) new Object[(int) elsize];
			buckets = new long[8];
		} else
			clear();
	}

	/**upper bound is O(buckets)</tt>*/
	public boolean isEmpty() {
		for (long bucket : buckets)
			if (elements(bucket) != 0)
				return false;
		return true;
	}

	/**{@inheritDoc}
	 * Time is O(buckets)*/
	public int size() {
		int size = 0;
		for (long bucket : buckets)
			size += elements(bucket);
		return size;
	}

	/**{@inheritDoc}
	 * Time is O(capacity)*/@Override
	public int hashCode() {
		int hash = 0;
		int i = -ew();
		while ((i = nextAfter(i))  !=  -1)
			hash += elements[i].hashCode();
		return hash;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null  ||  this.getClass() != obj.getClass())
			return false;
		LeanHash<?> lh = (LeanHash<?>)obj;

		//make internal structures equal
		optimize();
		lh.optimize();
		if (! Arrays.equals(buckets, lh.buckets))
			return false;
		if (elements.length != lh.elements.length)
			return false;

		int i = -ew();
		while ((i = nextAfter(i))  !=  -1)
			if (! elements[i].equals(lh.elements[i]))
				return false;
		return true;
	}


	public abstract LeanHash<E> clone();
	private static final long serialVersionUID = 1L;



	/**For inner classes.
	 *@return {@code this}*/
	protected LeanHash<E> self() {
		return this;
	}
	
	/**{@inheritDoc}
	 * A base for all iterators, Is bounded by capacity not size.
	 * {@code if (hasNext())remove()} will
	 *Supports <tt>remove()</tt>.*/
	protected class Iter<T> implements Iterator<T> {
		public static final byte READY=0, TAKEN=1, STARTED=2, REMOVED=3, ENDED=4;
		private int pos;
		private byte state = STARTED;
		protected Iter(int before_start) {
			pos = before_start;//so the first hasNext() works
		}
		public String toString() {
			return ""+pos+": "+self().toString();
		}
		protected T value(int index) {
			return (T)elements[index];
		}
		public int lastIndex() {
			if (state < REMOVED)
				return state;
			return -1;
		}

		@Override public final boolean hasNext() {switch (state) {
		case READY: return true;
		case ENDED: return false;
		default:
			pos = nextAfter(pos);
			if (pos == -1)
				state = ENDED;
			else
				state = READY;
			return hasNext();
		}}
		@Override public T next() {
			if (! hasNext())
				throw new NoSuchElementException();
			state = TAKEN;
			return value(pos);
		}
		@Override public void remove() {switch (state) {
		case STARTED: throw new IllegalStateException("Must call hasNext() first, Why do you want to remove the first element of an unknown-order iterator anyway?");
		case REMOVED: throw new IllegalStateException("Already removed element.");
		case   ENDED: throw new IllegalStateException("hasNext() returned false.");//The unit tests require IllegalStateException
		default://the unit tests require hasNext() remove(), even tough that only makes sense with known order.
			state = REMOVED;
			remove_index(hash(elements[pos]), pos);
		}}
	}


	/**{@inheritDoc}
	 * A base for all iterators, Is bounded by capacity not size.
	 * {@code if (hasNext())remove()} will
	 *Supports <tt>remove()</tt>.*/
	protected class OldIter<T> implements Iterator<T> {
		public static final int PREV_REMOVED = -3;//less than -ew()
		public static final int PREV_INVALID = -4;
		private int prev, next;
		protected OldIter(int before_start) {
			prev = next = before_start;//so the first hasNext() works
		}
		protected T value(int index) {
			return (T)elements[index];
		}
		/**Should only be called next(); returns -1 when called after hasNext() and remove().*/
		public int lastIndex() {
			if (prev < 0)
				return -1;
			return prev;
		}

		@Override public final boolean hasNext() {
			if (next == prev) {
				next = nextAfter(next);
				if (next == -1)
					return false;
			} else if (next < 0)
				return false;
			prev = PREV_INVALID;
			return true;
		}
		@Override public T next() {
			if (! hasNext())
				throw new NoSuchElementException();
			prev = next;
			return value(next);
		}
		@Override public void remove() throws IllegalStateException {
			if (prev < 0) {
				String error = "Must be called after next().";
				if (prev == PREV_REMOVED)
					error = "Already removed element.";
				throw new IllegalStateException(error);
			}

			remove_index(hash(elements[prev]), prev);
			hasNext();
			prev = PREV_REMOVED;
		}
	}
}
