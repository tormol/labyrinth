package tbm.util.collections;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**A collection, nothing more, nothing less.
 * Supports null elements and doesn't use equals() or hashCode().*/
public class ArrayCollection<E> extends AbstractCollection<E> {
	/**is also used by other classes in tbm.util.collections
	 *.toString() returns {@code "empty"}
	 *.equals() returns {@code false}*/
	protected static final Object empty = new Object() {
		@Override public String toString() {
			return "empty";
		}
		@Override public boolean equals(Object o) {
			return false;
		}
	};

	protected static final int default_size = 8;

	protected Object[] elements;
	protected int size;
	/**index of an unused slot, or -1 if */
	protected int anEmpty;

	protected ArrayCollection(Object[] elements, int size, int anEmpty) {
		this.elements = elements;
		this.size = size;
		this.anEmpty = anEmpty;
	}
	public ArrayCollection(Collection<E> c) {
		elements = c.toArray();
		size = elements.length;
		anEmpty = -1;
	}
	@SafeVarargs//couldn't be safer
	public ArrayCollection(E... elements) {
		this(elements, elements.length, -1);
	}
	public ArrayCollection() {
		this(new Objects[default_size],  default_size - 1,  0);
	}


	/**uses {@code .equal()}*/
	protected int indexOf(int begin, Object o) {
		if (o == null)
			return indexOfRef(begin, o);
		while (begin < elements.length)
			if (o.equals(elements[begin]))
				return begin;
			else
				begin++;
		return -1;
	}
	/**uses {@code ==}*/
	protected int indexOfRef(int begin, Object o) {
		while (begin < elements.length)
			if (elements[begin] == o)
				return begin;
			else
				begin++;
		return -1;
	}
	public boolean resize(int free) {
		if (elements.length < 4  ||  elements.length-size == free)
			return false;
		elements = toArray(free);
		anEmpty = free-1;
		return true;
	}

	@Override
	public int size() {
		return size;
	}
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(0, o) != -1;
	}
	public boolean containsRef(Object o) {
		return indexOfRef(0, o) != -1;
	}

	@Override
	public boolean add(E e) {
		if (anEmpty != -1) {
			elements[anEmpty] = e;
			anEmpty = -1;
		} else if (size == elements.length) {
			int new_size = (size * 3) / 2; 
			Object[] new_elements = new Object[new_size];
			int diff = new_size - size;
			//copy existing to the end to speed up finding empty slots.
			Arrays.fill(new_elements, 0, diff-1, empty);
			System.arraycopy(elements, 0, new_elements, diff, size);
			elements = new_elements;
			new_elements[diff-1] = e;
			anEmpty = diff - 2;
		} else
			elements[indexOfRef(0, empty)] = e;
		size++;
		return true;
	}

	protected boolean remove(int index) {
		if (index == -1)
			return false;
		elements[index] = empty;
		size--;
		if (anEmpty < index)
			anEmpty = index;
		//TODO: shrink
		return true;
	}
	@Override
	public boolean remove(Object o) {
		return remove(indexOf(0, o));
	}
	public int removeCompletely(Object o) {
		int i=0, next=indexOf(0, o);
		while (next == -1) {
			remove(next);
			i++;
			next = indexOf(next+1, o);
		}
		return i;
	}
	@Override
	public boolean removeAll(Collection<?> col) {
		Objects.requireNonNull(col);
		boolean modified = false;
		for (Object o : col)
			if (removeCompletely(o) > 0)
				modified = true;
		return modified;
	}

	public boolean removeRef(Object o) {
		return remove(indexOfRef(0, o));
	}
	public int removeRefCompletely(Object o) {
		int i=0, next=indexOf(0, o);
		while (removeRef(next)) {
			i++;
			next = indexOf(next+1, o);
		}
		return i;
	}
	public boolean removeAllRef(Collection<?> col) {
		Objects.requireNonNull(col);
		boolean modified = false;
		for (Object oe : col)
			if (removeRefCompletely(oe) > 0)
				modified = true;
		return modified;
	}

	@Override
	public void clear() {
		if (elements.length > 2*default_size)
			elements = new Object[default_size];
		else
			Arrays.fill(elements, empty);
	}

	@Override public arrayIterators.SkipEmpty<E, Object> iterator() {//might work
		return new arrayIterators.SkipEmpty<E, Object>((E[])elements, empty);
	}

	/**Is used to grow the internal array*/
	protected Object[] toArray(int free) throws IllegalArgumentException {
		if (free < 0)
			throw new IllegalArgumentException("free cannot be less than zero, but is "+free+'.');
		Object[] new_elements = new Object[size()+free];
		int to = new_elements.length;
		for (Object o : elements)
			if (o != empty) {
				to--;
				new_elements[to] = o;
			}
		Arrays.fill(new_elements, 0, to, empty);
		return new_elements;
	}
	@Override public Object[] toArray() {
		return toArray(0);
	}

	@SuppressWarnings("unchecked")//caller knows
	@Override public <T> T[] toArray(T[] array) {
		if (array.length < size())
			return (T[]) toArray();
		System.arraycopy(toArray(array.length-size()), 0, array, 0, array.length);
		return array;
	}
}
