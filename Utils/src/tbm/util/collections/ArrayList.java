package tbm.util.collections;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;

/**for Dat200
 *@author tbm
 *@eprecated use java.util.ArrayList
 */
public class ArrayList<E> implements List<E>, RandomAccess, Serializable, Cloneable {
	protected static final int default_size = 8;

	protected Object[] elements;
	/**indexes that are in use*/
	protected int start = 0;
	protected int end = 0;
	/**to throw an exception if someone tries to remove an element while iterating*/
	protected int modCount;

	@SafeVarargs
	public ArrayList(E... elements) {
		this.elements = elements;
	}
	public ArrayList() {
		this(default_size);
	}
	public ArrayList(int capacity) {try {
		elements = new Object[capacity];
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException("capacity cannot be negative but was "+capacity);
	}}
	public ArrayList(Collection<? extends E> col) {
		elements = col.toArray();
		end = elements.length;
	}
	/**used by subList()*/
	protected ArrayList(Object[] whole, int start, int end) {
		elements = whole;
		this.start = start;
		this.end = end;
	}

	@Override public int size() {
		return end - start;
	}
	@Override public boolean isEmpty() {
		return start == end;
	}

	/**@param index untrusted parameter to a list-specific method
	 *@return index + start
	 *@throws IndexOutOfBoundsException if not 0 <= index < size()*/
	protected int absoluteIndex(int index, boolean canAdd) {
		if (index < 0)
			throw new IndexOutOfBoundsException("index cannot be negative but was "+index);
		if (index > size()  ||  (!canAdd && index == size()))
			throw new IndexOutOfBoundsException("index must be less than size "+size()+" but was "+index);
		return index + start;
	}
	/**@return index of object in elements[] or -1 if not found*/
	protected int absoluteIndexOf(Object o) {
		for (int i=start; i<end; i++)
			if (Objects.equals(elements[i], o))
				return i;
		return -1;
	}
	/**fill a range of elements[] with nulls*/
	protected void clear(int from, int to) {
		Arrays.fill(elements, from, to, null);
	}

	@Override public E get(int index) {
		return (E) elements[absoluteIndex(index, false)];
	}
	@Override public final int indexOf(Object o) {
		int index = absoluteIndexOf(o);
		if (index != -1)
			index -= start;
		return index;
	}
	@Override public int lastIndexOf(Object o) {
		for (int i=end-1; i>=start; i--)
			if (Objects.equals(elements[i], o))
				return i-start;
		return -1;		
	}
	@Override public final boolean contains(Object o) {
		return absoluteIndexOf(o) != -1;
	}
	@Override public boolean containsAll(Collection<?> col) {
		for (Object e : col)
			if ( !contains(e))
				return false;
		return true;
	}

	@Override public void clear() {
		clear(start, end);
		start = end = 0;
		modCount++;
	}
	@Override public E remove(int index) {
		int absIndex = absoluteIndex(index, false);
		E removed = (E) elements[absIndex];
		modCount++;//absoluteIndex() didn't throw

		if (index < 0.3*size()) {//move everything before
			System.arraycopy(elements, start, elements, start+1, index);
			elements[start] = null;
			start++;
		} else {//move everything after
			if (size()-1-absIndex > 0)//arraycopy validates array indexes even if length is zero
				System.arraycopy(elements, absIndex+1, elements, absIndex, size()-1-absIndex);
			end--;
			elements[end] = null;
		}
		return removed;
	}
	@Override public boolean remove(Object o) {
		int index = absoluteIndexOf(o);
		if (index == -1)
			return false;
		remove(index+start);
		return true;
	}
	@Override public boolean removeAll(Collection<?> col) {
		int old_start = start;
		while (start < end  &&  col.contains(elements[start]))
			start++;
		clear(old_start, start);
		
		int old_end = end;
		while (end > start  &&  col.contains(elements[end-1]))
			end--;
		clear(end, old_end);

		modCount += start-old_start + old_end-end;
		boolean modified = old_start != start  ||  old_end != end;

		for (int i=start; i<end; i++)
			if (col.contains(elements[i])) {
				remove(i-start);
				modified = true;
				i--;
			}
		return modified;
	}
	@Override public boolean retainAll(Collection<?> col) {
		int oldModCount = modCount;
		//move the retained elements to the start and clear everything after
		int new_size = 0;
		for (int i=start; i<end; i++)
			if (col.contains(elements[i]))
				elements[new_size++] = elements[i];
			else
				modCount++;
		if (new_size+1 < elements.length/4)
			elements = Arrays.copyOf(elements, elements.length/3);
		else
			clear(new_size>start ? new_size : start,  end);
		start = 0;
		end = new_size;
		return oldModCount != modCount;
	}

	protected int findSpace(int relativeIndex, int n) {
		modCount += n;
		absoluteIndex(relativeIndex, true);
		if (relativeIndex == 0  &&  start >= n)
			return start -= n;
		if (relativeIndex == size()  &&  elements.length-end >= n) {
			int freeStart = end;
			end += n;
			return freeStart;
		}
		Object[] old = elements;
		if (end == elements.length  &&  start <= elements.length/3)//then grow
			elements = (Object[]) Array.newInstance(elements.getClass().getComponentType(), n + 2*elements.length);
		int newStart = (elements.length-size()-n) / 4;
		if (relativeIndex != 0)//would still check indexes and throw
			System.arraycopy(old, start, elements, newStart, relativeIndex);//before
		int freeStart = newStart + relativeIndex;
		if (relativeIndex != size())//would still check indexes and throw
			System.arraycopy(old, start+relativeIndex, elements, freeStart+n, size()-relativeIndex);//after
		end = end + n + newStart - start;
		start = newStart;
		return freeStart;
	}
	@Override public void add(int insertAt, E e) {
		insertAt = findSpace(insertAt, 1);
		elements[insertAt] = e;
		//if I write elements[findSpace()] add() might load elements before findSpace replaces it.  
	}
	@Override public final boolean add(E e) {
		add(size(), e);
		return true;
	}
	@Override public final boolean addAll(Collection<? extends E> col) {
		return addAll(size(), col);
	}
	@Override public boolean addAll(int insertAt, Collection<? extends E> col) {
		//works even if col is concurrent
		Object[] toAdd = col.toArray();
		insertAt = findSpace(insertAt, toAdd.length);
		System.arraycopy(toAdd, 0, elements, insertAt, toAdd.length);
		return toAdd.length != 0;
	}
	@Override public E set(int index, E e) {
		index = absoluteIndex(index, false);
		@SuppressWarnings("unchecked")
		E prev = (E) elements[index];
		elements[index] = e;
		return prev;
	}

	@Override public String toString() {
		if (isEmpty())
			return "[]";
		StringBuilder sb = new StringBuilder(size()*5);
		sb.append('[');
		for (int i=start; i<end; i++)
			sb.append(elements[i]).append(',').append(' ');
		sb.delete(sb.length()-2, sb.length());
		return sb.append(']').toString();
	}
	@Override public int hashCode() {
		int hashCode = 1;
		for (int i=start; i<end; i++)
			hashCode = 31*hashCode + (elements[i]==null ? 0 : elements[i].hashCode());//from the List javadoc
		return hashCode;
	}
	@Override public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null  ||  !(o instanceof List<?>))
			return false;
		List<?> other = (List<?>)o;
		if (this.size() != other.size())//
			return false;
		if ( !(other instanceof RandomAccess)) {
			int i = start;
			for (Object e : other)
				if ( !Objects.equals(elements[i], e))
					return false;
			return true;
		}
		//TODO if (other instanceof tbm.util.collections.ArrayList<?>)
		for (int i=0; i<size(); i++)
			if ( !Objects.equals(this.get(i), other.get(i)))
				return false;
		return true;
	}
	@SuppressWarnings("unchecked")
	@Override public ArrayList<E> clone() {try {
		ArrayList<E> clone = (ArrayList<E>) super.clone();
		clone.elements = clone.elements.clone();
		return clone;
	} catch (CloneNotSupportedException e) {
		e.printStackTrace();
		return new ArrayList<E>(elements.clone(), start, end);
	}}

	@SuppressWarnings("unchecked")
	@Override public <T> T[] toArray(T[] array) {
		if (array.length < size())
			return toArray((Class<T[]>)array.getClass());
		System.arraycopy(elements, start, array, 0, size());
		Arrays.fill(array, size(), array.length, null);
		return array;
	}
	@Override public Object[] toArray() {
		return toArray(Object[].class);
	}
	//makes more sense than toArray(T[])
	public <T> T[] toArray(Class<T[]> ofType) {
		return Arrays.copyOfRange(elements, start, end, ofType);
	}

	/**{@inheritDoc}
	 *@return a view that cannot be structurally modified, use sublist().clone() if you need to*/
	@Override public List<E> subList(int from, int to) {
		if (from > to)
			throw new IllegalArgumentException("from > to");
		return new ArrayList<E>(elements, absoluteIndex(from, true), absoluteIndex(to, true)) {
			@Override protected int findSpace(int relativeIndex, int n) {
				throw new UnsupportedOperationException();
			}
			@Override protected void clear(int start, int end) {
				throw new UnsupportedOperationException();
			}
			@Override public E remove(int relativeIndex) {
				throw new UnsupportedOperationException();
			}
			@Override public boolean retainAll(Collection<?> col) {
				throw new UnsupportedOperationException();
			}
		};
	}
	@Override public void forEach(Consumer<? super E> consumer) {
		for (int i=start; i<end; i++)
			consumer.accept((E) elements[i]);
	}
	@Override public final Iterator<E> iterator() {
		return listIterator();
	}
	@Override public final ListIterator<E> listIterator() {
		return listIterator(0);
	}
	@Override public ListIterator<E> listIterator(int startAt) {
		absoluteIndex(startAt, true);
		return new ArrayListIterator(startAt);
	}


	private static final long serialVersionUID = 1;



	protected class ArrayListIterator implements ListIterator<E> {
		protected int modCount = ArrayList.this.modCount;
		/**position of the last element returned by next, starts at -1 and can be larger than array.length*/
		protected int pos;
		protected boolean forward = true;
		protected boolean canRemove = false;

		protected ArrayListIterator(int pos) throws IndexOutOfBoundsException {
			this.pos = pos-1;//sfirst nextIndex() must be 0
		}

		protected final void checkModCount() throws ConcurrentModificationException {
			if (ArrayList.this.modCount != modCount)
				throw new ConcurrentModificationException();
		}

		/**return the next index to be returned, or >= array.length if at the end*/
		@Override public int nextIndex() {
			return pos + (forward ? 1 : 0);
		}
		/**return index of the last element returned by next()*/
		@Override public int previousIndex() {
			return pos - (forward ? 0 : 1);
		}

		@Override public final boolean hasNext() {
			return nextIndex() < size();
		}
		/**@return true if <tt>next()</tt> has been called*/
		@Override public final boolean hasPrevious() {
			return previousIndex() >= 0;
		}

		/**get the element at pos
		 *@param update_pos should {@code this.pos} be set to pos if pos is valid?
		 *@throws NoSuchElementException if pos is an invalid index*/
		protected E get(int pos, boolean update_pos) {
			try {
				checkModCount();
				E e = (E) elements[absoluteIndex(pos, false)];//fail before changing position or direction
				if (update_pos)
					this.pos = pos;
				canRemove = true;
				return e;
			} catch (IndexOutOfBoundsException ioobe) {
				throw new NoSuchElementException();
			}
		}

		/**Return the next element but don't advance the iteration
		 *@throws NoSuchElementException if there are no more elements.
		 * (returning null would be meaningless if there can be null elements in the array,
		 * and {@code if (hasNext() && peek().someFunc())} is clearer than {@code if (peek() != null  &&  peek().somefunc())}
		 */
		public final E peekNext() {
			return get(nextIndex(), false);
		}
		public final E peekPrevious() {
			return get(previousIndex(), false);
		}

		@Override public E next() {
			E e = get(nextIndex(), true);
			forward = true;
			return e;
		}
		/**{@inheritDoc}
		 *Is idempotent when NoSuchElementException is thrown*/
		@Override public E previous() {
			E e = get(previousIndex(), true);
			forward = false;
			return e;
		}

		@Override public void add(E e) {
			checkModCount();
			pos = nextIndex();
			ArrayList.this.add(pos,  e);
			forward = true;
			modCount = ArrayList.this.modCount;
			canRemove = false;
		}

		@Override public void set(E e) {
			checkModCount();
			if ( !canRemove)
				throw new IllegalStateException();
			ArrayList.this.set(pos, e);
		}

		@Override public void remove() {
			checkModCount();
			if ( !canRemove)
				throw new IllegalStateException();
			canRemove = false;
			ArrayList.this.remove(pos);
			pos--;
			if (pos == -1)//removing the first element is the only way to set pos to -1, and -1,backward breaks nextIndex()
				forward = true;
			modCount = ArrayList.this.modCount;
		}
	}


	/*//guava's IteratorTester is hard to follow
	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<>(Arrays.asList("t"));
		ListIterator<String> iter = list.listIterator();
		System.out.println(iter.hasNext());
		iter.add("e");
		//try {System.out.println(iter.previous());}
		//	catch (NoSuchElementException e) {}
		//iter.remove();
		iter.add("q");
		System.out.println(iter.hasNext());
		try {System.out.println(iter.previous());}
			catch (NoSuchElementException e) {}
		iter.remove();
		System.out.println(list);
	}*/
}
