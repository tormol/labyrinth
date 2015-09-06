package tbm.util.collections;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**for Dat200
 *@author tbm
 *@eprecated use java.util.LinkedList
 */
public class LinkedList<E> implements List<E>, Serializable, Cloneable {
	protected static class Link<E> implements Serializable {
		protected Link<E> prev, next;
		protected E element;
		protected Link(Link<E> prev, Link<E> next, E elements) {
			this.prev = prev;
			this.next = next;
			this.element = elements;
		}
		private static final long serialVersionUID = 1L;
	}

	//would be nice to have those methods inside Link, but I need List instance too
	/**@param index must be validated first*/
	protected Link<E> getLink(int index) {
		if (index == size)
			return null;

		Link<E> l;
		if (size-index < index)//start from end
			for (l = last;  index+1 != size;  l = l.prev)
				index++;
		else//start from start
			for (l = first;  index != 0;  l = l.next)
				index--;
		return l;
	}
	protected Link<E> getLink(Object o, boolean fromEnd) {
		if (fromEnd) {
			for (Link<E> l=last;  l != null;  l = l.prev)
				if (Objects.equals(l.element, o))
					return l;
		} else {
			for (Link<E> l=first;  l != null;  l = l.next)
				if (Objects.equals(l.element, o))
					return l;
		}
		return null;
	}

	protected Link<E> insertLink(Link<E> before, E e) {
		size++;
		modCount++;
		if (before == null)
			if (size == 1)//then it was zero three lines up
				return first = last = new Link<>(null, null, e);
			else
				return last = last.next = new Link<>(last, null, e);
		if (before == first)
			return first = before.prev = new Link<>(null, first, e);
		return before.prev = before.prev.next = new Link<>(before.prev, before, e);
	}
	protected E removeLink(Link<E> remove) {
		if (remove.next == null)
			last = last.prev;
		else
			remove.next.prev = remove.prev;
		if (remove.prev == null)
			first = first.next;
		else
			remove.prev.next = remove.next;

		size--;
		modCount++;
		return remove.element;
	}




	protected Link<E> first, last;
	protected int size;
	/**to throw an exception if someone tries to remove an element while iterating
	 * Actually unused as the Iterator seems pretty safe.*/
	protected int modCount;

	public LinkedList() {
		clear();
	}
	public LinkedList(Collection<? extends E> col) {
		this();
		addAll(col);
	}
	/**used by subList()*/
	protected LinkedList(Link<E> first, Link<E> last, int size) {
		this.first = first;
		this.last = last;
		this.size = size;
	}

	@Override public final int size() {
		return size;
	}
	@Override public final boolean isEmpty() {
		return size == 0;
	}

	/**@param index untrusted parameter to a list-specific method
	 *@return index
	 *@throws IndexOutOfBoundsException if not 0 <= index < size()*/
	protected int validIndex(int index, boolean canAdd) {
		if (index < 0)
			throw new IndexOutOfBoundsException("index cannot be negative but was "+index);
		if (index > size  ||  (!canAdd && index == size))
			throw new IndexOutOfBoundsException("index must be less than size "+size()+" but was "+index);
		return index;
	}

	@Override public E get(int index) {
		index = validIndex(index, false);
		return getLink(index).element;
	}
	@Override public int indexOf(Object o) {
		int index = 0;
		for (Link<E> l=first;  l != null;  l = l.next)
			if (Objects.equals(l.element, o))
				return index;
			else
				index++;
		return -1;
	}
	@Override public int lastIndexOf(Object o) {
		int index = size-1;
		for (Link<E> l=last;  l != null;  l = l.prev)
			if (Objects.equals(l.element, o))
				return index;
			else
				index--;
		return -1;
	}
	@Override public final boolean contains(Object o) {
		return indexOf(o) != -1;
	}
	@Override public boolean containsAll(Collection<?> col) {
		for (Object e : col)
			if ( !contains(e))
				return false;
		return true;
	}

	@Override public void clear() {
		first = last = null;
		modCount += size;
		size = 0;
	}
	@Override public E remove(int index) {
		return removeLink(getLink(validIndex(index, false)));
	}

	@Override public boolean remove(Object o) {
		Link<E> l = getLink(o, false);
		if (l != null)
			removeLink(l);
		return l != null;
	}
	@Override public boolean removeIf(Predicate<? super E> cond) {
		boolean modified = false;
		for (Iterator<E> iter = iterator();  iter.hasNext();)
			if (cond.test(iter.next())) {
				iter.remove();
				modCount++;
				modified = true;
			}
		return modified;
	}
	@Override public boolean removeAll(Collection<?> col) {
		return removeIf(e->col.contains(e));
	}
	@Override public boolean retainAll(Collection<?> col) {
		return removeIf(e->!col.contains(e));
	}

	@Override public void add(int insertAt, E e) {
		insertLink(getLink(validIndex(insertAt, true)), e);
	}
	@Override public boolean add(E e) {
		insertLink(null, e);
		return true;
	}
	@Override public final boolean addAll(Collection<? extends E> col) {
		for (E e : col)
			insertLink(null, e);
		return !col.isEmpty();
	}
	@Override public boolean addAll(int insertAt, Collection<? extends E> col) {
		Link<E> l = insertAt==size ? null : getLink(validIndex(insertAt, false));
		for (E e : col)
			insertLink(l, e);
		return !col.isEmpty();
	}
	@Override public E set(int index, E e) {
		Link<E> l = getLink(validIndex(index, false));
		E prev = l.element;
		l.element = e;
		return prev;
	}

	@Override public String toString() {
		if (isEmpty())
			return "[]";
		StringBuilder sb = new StringBuilder(size*5);
		sb.append('[');
		for (Link<E> l=first;  l != null;  l = l.next)
			sb.append(l.element).append(',').append(' ');
		sb.delete(sb.length()-2, sb.length());
		return sb.append(']').toString();
	}
	@Override public int hashCode() {
		int hashCode = 1;
		for (Link<E> l=first;  l != null;  l = l.next)
			hashCode = 31*hashCode + (l.element==null ? 0 : l.element.hashCode());//from the List javadoc
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
		if (other instanceof RandomAccess) {
			int i=0;
			for (Link<E> l=first;  l != null;  l = l.next)
				if ( !Objects.equals(l.element, other.get(i++)))
					return false;
			return true;
		}
		Iterator<?> oiter = other.iterator();
		for (Link<E> l=first;  l != null;  l = l.next)
			if ( !Objects.equals(l.element, oiter.next()))
				return false;
		return true;
	}
	@Override public LinkedList<E> clone() {try {
		@SuppressWarnings("unchecked")
		LinkedList<E> clone = (LinkedList<E>) super.clone();
		clone.clear();
		clone.addAll(this);
		return clone;
	} catch (CloneNotSupportedException e) {
		e.printStackTrace();
		return new LinkedList<E>(this);
	}}

	@SuppressWarnings("unchecked")
	@Override public <T> T[] toArray(T[] array) {
		if (array.length < size)
			return toArray((Class<T[]>)array.getClass());
		int i=0;
		for (Link<E> l=first;  l != null;  l = l.next)
			array[i++] = (T) l.element;
		Arrays.fill(array, size, array.length, null);
		return array;
	}
	@Override public Object[] toArray() {
		return toArray(Object[].class);
	}
	//makes more sense than toArray(T[])
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(Class<T[]> ofType) {
		T[] array = (T[]) Array.newInstance(ofType.getComponentType(), size);
		int i=0;
		for (Link<E> l=first;  l != null;  l = l.next)
			array[i++] = (T) l.element;
		return array;
	}

	/**{@inheritDoc}
	 *@throws unsupportedOperationException always, because sublists are hard*/
	@Override public List<E> subList(int from, int to) {
		if (validIndex(from, true)  >  validIndex(to, true))
			throw new IllegalArgumentException("from > to");
		throw new UnsupportedOperationException("sublists are hard");
	}
	@Override public void forEach(Consumer<? super E> consumer) {
		for (Link<E> l = first;  l != null;  l = l.next)
			consumer.accept(l.element);
	}
	@Override public Iterator<E> iterator() {
		return new LinkedListIterator(first);
	}
	@Override public ListIterator<E> listIterator() {
		return new LinkedListIterator(first);
	}
	@Override public ListIterator<E> listIterator(int startAt) {
		return new LinkedListIterator( getLink( validIndex(startAt, true) ));
	}

	private static final long serialVersionUID = 1;


	protected class LinkedListIterator implements ListIterator<E> {
		protected static final byte FORWARD = 1;
		protected static final byte BACKWARD = -1;
		protected static final byte REMOVED = 0;

		protected Link<E> prev = null;
		protected Link<E> next;
		protected byte state = FORWARD;
		protected LinkedListIterator(Link<E> first) {
			next = first;
		}

		protected Link<E> nextLink() {
			if (prev != null)
				next = prev.next;
			return next;
		}
		protected Link<E> previousLink() {
			if (next != null)
				prev = next.prev;
			return prev;
		}

		@Override public boolean hasNext() {
			return nextLink() != null;
		}
		@Override public boolean hasPrevious() {
			return previousLink() != null;
		}

		public E peekNext() {try {
			return nextLink().element;
		} catch (NullPointerException npe) {
			throw new NoSuchElementException();
		}}
		public E peekPrevious() {try {
			return previousLink().element;
		} catch (NullPointerException npe) {
			throw new NoSuchElementException();
		}}
		@Override public E next() {
			if ( !hasNext())
				throw new NoSuchElementException();
			prev = next;
			next = next.next;
			state = FORWARD;
			return prev.element;
		}
		@Override public E previous() {
			if ( !hasPrevious())
				throw new NoSuchElementException();
			next = prev;
			prev = prev.prev;
			state = BACKWARD;
			return next.element;
		}

		protected int indexOf(Link<E> needle) {
			int i = 0;
			for (Link<E> l = first;  l != needle;  l=l.next)
				i++;
			return i;
		}
		@Override public int nextIndex() {
			return     nextLink() == null  ?  size  :  indexOf(next);
		}
		@Override public int previousIndex() {
			return previousLink() == null  ?   -1   :  indexOf(prev);
		}

		protected Link<E> lastLink() {switch (state) {
			case  FORWARD: return prev;
			case BACKWARD: return next;
			case  REMOVED: return null;
			default: throw new IllegalStateException("Internal bug: unknown state: "+state);
		}}
		@Override public void remove() {
			Link<E> last = lastLink();
			if (last == null)
				throw new IllegalStateException();

			switch (state) {
				case  FORWARD: prev = prev.prev; break;
				case BACKWARD: next = next.next; break;
			}
			state = REMOVED;
			LinkedList.this.removeLink(last);
		}
		@Override public void add(E e) {
			prev = insertLink(next, e);
			state = REMOVED;
		}
		public void set(E e) {try {
			lastLink().element = e;
		} catch (NullPointerException npe) {
			throw new IllegalStateException();
		}}
	}


	//all loops that currently use l==null must use i<size
	//and toString, Iterators must also be fixed
	//begin and endmarkers would simplify remove and add, but doesn't solve sublists,
	// as sublist().last.prev.next will point to the next element in the backing list
	protected static class LinkedSubList<E> extends LinkedList<E> {
//		return new LinkedSubList<E>(from==to ? null : getLink(from),
//		                            from==to ? null : getLink(to-1),
//		                            to - from);
		@Override protected Link<E> insertLink(Link<E> before, E e) {
			throw new UnsupportedOperationException();
		}
		@Override protected E removeLink(Link<E> l) {
			throw new UnsupportedOperationException();
		}
		@Override public void clear() {
			throw new UnsupportedOperationException();
		}
		@Override public int indexOf(Object o) {
			int index = super.indexOf(o);
			return index<size ? index : -1;//might go past end
		}
		@Override public int lastIndexOf(Object o) {
			int index = super.lastIndexOf(o);
			return index>=0 ? index : -1;//might go past start
		}
		private static final long serialVersionUID = 1L;
	}
}
