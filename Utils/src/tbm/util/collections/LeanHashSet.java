package tbm.util.collections;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class LeanHashSet<E> extends LeanHash<E> implements Set<E> {
	public static <E> LeanHashSet<E> copy(Collection<E> c) {
		if (c instanceof LeanHashSet)
			return ((LeanHashSet<E>)c).clone();
		return new LeanHashSet<E>(c);
	}


	public LeanHashSet(int initialCapacity, float ratio) {
		super(initialCapacity, ratio);
	}

	protected LeanHashSet(E[] elements, long[] buckets) {
		super(elements, buckets);
	}

	/**If c might be a LeanHashSet, use LeanHashSet.copy(c)*/
	public LeanHashSet(Collection<E> c) {
		super(requireNonNull(c).size(), default_ratio);
		addAll(c);
	}


	@Override//Leanhash
	protected final int ew() {
		return 1;
	}


	@Override//Set
	public boolean add(E e) {
		long hash_index = indexOf(e);
		if (index(hash_index) != -1)
			return false;
		add_new(e, hash(hash_index));
		return true;
	}

	@Override//Set
	public boolean contains(Object obj) {
		if (obj == null)
			return false;
		return index(indexOf(obj)) != -1;
	}

	@Override//Set
	public boolean remove(Object obj) {
		return remove_element(obj) != -1;
	}


	@Override//Set
	public boolean addAll(Collection<? extends E> col) {
		boolean modified = false;
		for (E e : col)
			modified |= add(e);
		return modified;
	}

	@Override//Set
	public boolean containsAll(Collection<?> col) {
		for (Object e : col)
			if (! this.contains(e))
				return false;
		return true;
	}

	@Override//Set
	public boolean removeAll(Collection<?> col) {
		boolean modified = false;
		for (Object e : col)
			modified |= remove(e);
		return modified;
	}

	@Override//Set
	public boolean retainAll(Collection<?> col) {
		boolean modified = false;
		int i = -ew();
		while ((i = nextAfter(i)) != -1)
			if (! col.contains(elements[i])) {
				remove_index(hash(elements[i]), i);
				modified = true;
			}
		return modified;
	}


	@Override//Set
	public Iterator<E> iterator() {
		return new Iter<E>(-1);
	}

	/**Uses <tt>o.contains()</tt>
	 * Is capacity-bound.*/@Override//LeanHash
	public boolean equals(Object o) {
		if (o instanceof LeanHashSet)
			return super.equals(o);
		if (! (o instanceof Set))
			return false;
		Set<?> set = (Set<?>)o;

		if (this.size() != set.size())
			return false;
		//The loop only checks that this is a subset of set, size
		int i = -ew();
		while ((i = nextAfter(i))  !=  -1)
			if (! set.contains(elements[i]))
				return false;
		return true;
	}




	@Override//LeanHash
	public LeanHashSet<E> clone() {
		return new LeanHashSet<E>(elements.clone(), buckets.clone());
	}

	@Override//Set
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (E el : elements)
			if (el != null)
				sb.append(el).append(',').append(' ');
		if (sb.length() > 1)//more than zero elements.
			sb.delete(sb.length()-2, sb.length());//remove last ", "
		sb.append(']');
		return sb.toString();
	}

	@Override//Set
	public Object[] toArray() {
		Object[] copy = new Object[size()];
		int put = copy.length;//FIXME: why opposite order?
		for (E el : elements)
			if (el != null) {
				put--;
				copy[put] = el;
			}
		return copy;
	}

	@SuppressWarnings("unchecked")
	@Override//Set
	public <T> T[] toArray(T[] copy) {
		Objects.requireNonNull(copy);
		int put = 0;//elements must be from start of array
		int from = elements.length;//FIXME: why opposite order?
		while (from > 0) {
			from--;
			if (elements[from] != null) {
				//then add to copy
				if (put == copy.length) {
					//array is too small
					int elements_left = 1;//elements[from]
					for (int i=0;  i<from;  i++)
						if (elements[i] != null)
							elements_left++;
					copy = Arrays.copyOf(copy, copy.length+elements_left);
				}
				copy[put] = (T)elements[from];
				put++;
			}
		}
		if (put < copy.length)
			Arrays.fill(copy, put, copy.length, null);
		return copy;
	}


	private static final long serialVersionUID = 1L;
}
