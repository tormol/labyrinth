/* Copyright 2016 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util.collections;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import no.torbmol.util.collections.randomAccessIterators.ModifiableOneWayListIterator;

/**
 *A base class to create memory efficient hash structures
 *Uses one array with elements and another to store hash information.
 *Since multiple entries can have the same hash, a separate buckets array is
 *used as lookup from a hash value, and store the first index to elements with
 *that hash, the number of those elements.
 *To avoid creating an object for each bucket, the two integers are stored as
 *one long, and extracted by bitwise operations, which I hope the JVM will optimize away
 *
 *Doesn't implement Collection since Map doesn't.
 */
abstract class LeanHash<E> implements Cloneable, Serializable {
	public static final int default_initial_buckets = 8;
	public static final int minimum_initial_buckets = 4;
	/**elements / buckets*/
	public static final float default_ratio = 1.25f;
	/**@return (int)(default_initial_buckets * default_ratio)*/
	public static final int default_initial_size() {
		return Math.round(default_initial_buckets * default_ratio);
	}

	/**struct{int entries, first_index}
	 * the 32 lsbs store the first index of elements with this hash
	 * and the otheer half store the number of elements with this hash*/
	protected long[] buckets;//struct{int entries, first_index}
	protected E[] elements;
	//for small sets, my guess is all the bucket operations makes this slower than a linear search, which would also need even less memory
	//TODO size, ew as a field and short modCount are probably worth it


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
	 */@SuppressWarnings("unchecked")
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
	//FIXME: will it be inlined?
	protected abstract int ew();


  //////////////////////////////////////////
 //bucket and other bit-twiddling methods//
//////////////////////////////////////////

	/**get the hash/bucket index out of the <tt>long</tt> returned by <tt>indexOf()</tt>*/
	protected final int hash(long hash_index) {
		return (int)(hash_index >> 32);
	}
	/**Get the number of elements in this bucket.
	 *@return {@code bucket>>32}*/
	protected final int elements(long bucket) {
		return (int)(bucket >> 32);
	}
	/**Get the elements[] index of the first element out of a long bucket.
	 *@param bucket can also be hash_index
	 *@return bucket & 0xffff_ffff*/
	protected final int index(long bucket) {
		return (int)(bucket & 0xffff_ffffL);
	}

	/**store two <tt>int</tt>s as a <tt>long</tt>
	 * Warning: addition of negative first will overflow into elements and increment it
	 *@return {@code (long)elements << 32  |  first_index}*/
	private long bucket(int elements, int first_index) {
		return ((long)elements << 32)  |  ((long)first_index & 0xffff_ffffL);
	}
	/**assert that elements is positive and start and end are within elements.length*/
	private void valid_bucket(long bucket) {
		assert !(elements(bucket) < 1  ||  index(bucket) < 0  ||  index(bucket)+elements(bucket)*ew() > elements.length)
		:"Invalid bucket: "+elements(bucket)+" elements, starts at "+index(bucket)+", elements.length: "+elements.length;
	}
	/**create a new bucket and validate it*/
	private long new_bucket(int entries, int first_index) {
		long bucket = bucket(entries, first_index);
		valid_bucket(bucket);
		return bucket;
	}



  ////////////////
 //get elements//
////////////////

	/**get the index of the bucket this object belongs to
	 *@param value must be non-null
	 *@return {@code value.hashCode() & (buckets.length-1)}*/
	public int hash(Object value) {
		return value.hashCode() & (buckets.length-1);
	}

	/**get the bucket index and index (if found) of the object
	 *
	 *@param o must be non-null*/
	protected final long indexOf(Object o) {//TODO rename to something more accurate
		requireNonNull(o, getClass().getName()+" cannot store null");
		int hash = hash(o);
		int from = index(buckets[hash]);
		int to = from + elements(buckets[hash]) * ew();
		for (int i = from;  i < to;  i += ew())
			if (elements[i].equals(o))
				return bucket(hash, i);
		return bucket(hash, -1);
	}



  ////////////////////
 //add new elements//
////////////////////

	/**Add a new element
	 *@param element must not be equal to another element, or null
	 *@param hash {@code hash(element)}
	 *@return index of element*/
	protected int add_new(E element, int hash) {
		if (element == this)
			throw new IllegalArgumentException("Cannot add itself.");//both Set and Map
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
			valid_bucket(buckets[hash] += (1L<<32)-ew());//increment entries and decrement index (negative index overflows addition and increments elements)
			return before;
		} else if (after < elements.length  &&  elements[after] == null) {
			valid_bucket(buckets[hash] += bucket(1, 0));//=1L<<32: increment entries
			return after;
		}

		//the element before or after is alone in its bucket, so move that
		else if (after < elements.length  &&  elements(buckets[hash(elements[after])]) == 1) {
			if (relocate_single(after, hash, bucket(1, 0)))
				return after;
		} else if (before >= 0  &&  elements(buckets[hash(elements[before])]) == 1) {
			if (relocate_single(before, hash, (1L<<32)-ew()))
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
	 *Code was used twice in <tt>fit()</tt>
	 *@param growing_hash hash of the new element
	 *@return true if the element could be relocated*/
	private boolean relocate_single(int to_move, int growing_hash, long add_to_growing_bucket) {
		for (int i=elements.length-ew();  i>=0;  i-=ew())
			if (elements[i] == null) {
				System.arraycopy(elements, to_move, elements, i, ew());
				buckets[hash(elements[i])] = new_bucket(1, i);
				valid_bucket(buckets[growing_hash] += add_to_growing_bucket);
				return true;
			}
		return false;
	}


	
  /////////////////////
 //growing/shrinking//
/////////////////////

	/**create a new array with the same type as elements without copying content
	 */@SuppressWarnings("unchecked")
	private E[] newElements(int length) {
		return (E[]) Array.newInstance(elements.getClass().getComponentType(), length);
	}


	/**Multiply the size of arrays with grow_ratio,
	 * and rehash since all buckets are split in two.
	 *@param double_and_spread double size of elements along with buckets, and try spread out the elements.
	 *@return a free slot if <tt>double_and_spread</tt> or <tt>elements.length</tt>.
	 */
	protected int grow_and_rehash(boolean double_and_spread) {
		E[] old_elements = elements;
		if (double_and_spread) {
			elements = newElements(old_elements.length * 2 );
			//for each old_bucket there might be added an empty slot
			if (elements.length/ew() <= 1+buckets.length)
				double_and_spread = false;
		} else
			elements = newElements(old_elements.length);//when splitting buckets elements will be moved around without a buffer
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
				next += ew();//leave some space between buckets
		}
		return next;
	}


	public boolean set_ratio(float ratio) throws IllegalArgumentException {
		if (ratio <= 0)
			throw new IllegalArgumentException("negative ratio: "+ratio);
		if (Math.abs(ratio - get_ratio()) < 0.05)
			return true;
		if (ratio > get_ratio()) {
			E[] new_elements = newElements((int)(buckets.length*ratio));
			System.arraycopy(elements, 0, new_elements, 0, elements.length);
			elements = (E[])new_elements;
			return true;
		}
		int size = size();
		if (buckets.length*ratio < size)
			return false;
		E[] new_elements = newElements(size);
		pack(new_elements);
		elements = new_elements;
		return true;
	}


	/**Move all elements to the start of the array, passing <tt>this.elements</tt> is safe.
	 *@return size*/
	private int pack(Object[] new_elements) {
		int to = 0;
		//copy everything until the first null
		while (to < elements.length  &&  elements[to] != null)
			to += ew();
		if (to != 0)
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
	 *@return true if anything was done, currently never*/
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
			E[] new_elements = newElements(size*ew());
			pack(new_elements);
			elements = new_elements;
		}
		return did_anything;
	}

	/**Untested*/
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
			new_elements = newElements(size);
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



  //////////
 //remove//
//////////

	/**Removes all elements, but does not reduce the capacity*/
	public void clear() {
		Arrays.fill(elements, null);
		Arrays.fill(buckets, 0);
	}

	/**Removes all elements reduce to 8 buckets and keep the ratio*/
	public void clear_and_shrink() {
		if (buckets.length > 8) {
			float elsize = 8 * elements.length / (float)buckets.length;
			elements = newElements((int) elsize);
			buckets = new long[8];
		} else
			clear();
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



  //////////////////////////////////////
 //methods shared between Set and Map//
//////////////////////////////////////

	public float get_ratio() {
		return (float)(elements.length / ew())  /  buckets.length;
	}

	public int capacity() {
		return elements.length;
	}

	/**{@inheritDoc}
	 * upper bound is O(buckets)</tt>*/
	public boolean isEmpty() {
		for (long bucket : buckets)
			if (elements(bucket) != 0)
				return false;
		return true;
	}

	/**{@inheritDoc}
	 * takes O(bucket) time*/
	public int size() {
		int size = 0;
		for (long bucket : buckets)
			size += elements(bucket);
		return size;
	}

	@Override public int hashCode() {
		int hashCode = 0;
		for (long bucket : buckets)
			if (elements(bucket) > 0)
				hashCode += elements(bucket) * elements[index(bucket)].hashCode();
		return hashCode;
	}

	@Override public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null  ||  o.getClass() != this.getClass())
			return false;
		LeanHash<?> other = (LeanHash<?>)o;

		//make internal structures equal
		this.optimize();
		other.optimize();

		if ( !Arrays.equals(this.buckets, other.buckets))
			return false;
		if (this.elements.length != other.elements.length)
			return false;

		for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))
			if (! this.elements[index(b_i)].equals(other.elements[index(b_i)]))
				return false;
		return true;
	}


	/**prints stackTrace and return null if super.clone() somehow fails*/
	@Override public LeanHash<E> clone() {
		try {
			@SuppressWarnings("unchecked")
			LeanHash<E> clone = (LeanHash<E>) super.clone();
			clone.elements = elements.clone();
			clone.buckets = buckets.clone();
			return clone;
		} catch  (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}



  ///////////////////////
 //a quittable foreach//
///////////////////////
	//uses the same hash_index format as indexOf()

	//for (long b_i = nextAfter_start();  index(b_i) != -1;  b_i = nextAfter(b_i))

	/**get the index of the first element of the next non-empty bucket after b*/
	private long nextAfter_bucket(int b) {
		for (b++;  b < buckets.length;  b++)
			if (elements(buckets[b]) != 0)
				return bucket(b, index(buckets[b]));
		return bucket(b, -1);
	}

	/**get the index of the first element of the first non-empty bucket after b*/
	protected final long nextAfter_start() {
		return nextAfter_bucket(-1);
	}

	/**get the index of the next element after i, or -1 if no more*/
	protected final long nextAfter(long b_i) {
		int index = index(b_i);
		int hash = hash(b_i);
		long bucket = buckets[hash];

		if (index + ew() - index(bucket)  <  elements(bucket) * ew())
			return bucket(hash, index+ew());//b_i+=ew()
		else
			return nextAfter_bucket(hash);
	}



	//TODO: bucket-based iterator, one problem: hash should start at -1, but there you can't see how many elements you have

	/**{@inheritDoc}
	 * A base for all iterators,
	 * Supports <tt>remove()</tt> but not <tt>set()</tt>.*/
	//ew() and canRemove makes this class different enough that extending ModifiableSkipEmpty would just give me more indirection
	protected class Iter<T> extends ModifiableOneWayListIterator<T> {
		/**nextIndex() cache*/
		protected int next = -1;
		boolean canRemove = false;
		protected Iter() {
			pos = -ew();//pos+=ew() == 0
		}
		public String toString() {
			return LeanHash.this.toString() + '['+pos+']';
		}

		@Override protected int maxIndex() {
			return elements.length;
		}
		@Override protected final int nextIndex() {//safer to reimplement than multiply or divide by ew() everywhere
			if (next == -1) {
				next = pos;
				do {
					next += ew();
				} while (next < elements.length  &&  elements[next] == null);
			}
			return next;
		}

		@Override public T next() {
			T e = super.next();
			next = -1;
			canRemove = true;
			return e;
		}

		@SuppressWarnings("unchecked")
		@Override protected T getIndex(int index) {
			return (T)elements[index];
		}
		@Override public final void remove() {
			if ( !canRemove)
				throw new IllegalStateException("no element to remove");
			remove_index(hash(elements[pos]), pos);
			pos -= ew();//if the element was in the middle of its bucket, a later element took its place
			next = -1; //else it is null and ignored
			canRemove = false;
		}
		@Override public void set(T e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();//if the new element would be put in a different bucket, it would have a new, possibly later index
		}
	}


	private static final long serialVersionUID = 1L;
}
