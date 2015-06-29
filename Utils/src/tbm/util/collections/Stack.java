package tbm.util.collections;
import java.util.AbstractList;
import java.util.Deque;
import java.util.Iterator;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**A memory efficient but dangerous version of LinkedList
 * instead of using an for loop, use {@code for (E e=stack.peek(); e!=null; e=e.next)}*/
public class Stack<E extends Stack.Stackable<E>> extends AbstractList<E> implements Deque<E> {
	public static class Stackable<E extends Stack.Stackable<E>> {
		protected E next = null;
		private Stackable(E next) {
			this.next = next;
		}
		protected Stackable()
			{}
	}
	private static final String nullNotSupported = "Stack cannot store null elements.";
	public static <E extends Stackable<E>> E getN(E top, int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("index < 0");
		for (E e=top; e!=null; e=e.next, index--)
			if (index == 0)
				return e;
		throw new IndexOutOfBoundsException("index >= size()");
	}
	public static int depth(Stackable<?> top) {
		if (top == null)
			return 0;
		int size=1;
		while ((top=top.next) != null)
			size++;
		return size;
	}
	protected static <E extends Stackable<E>> E replaceNext(E e, E newNext) {
		E oldNext = e.next;
		e.next = newNext;
		return oldNext;
	}


	protected E top;

	public Stack() {
		top = null;
	}
	public Stack(E e) {
		top = e;
	}
	/**Adds elements in the same order as the collection, ie oppositie of push()*/
	public Stack(Iterable<E> c) {
		Stackable<E> startref = new Stackable<E>();
		Stackable<E> prev = startref;
		for (E e : c)
			if (e != null) {
				prev.next = e;
				prev = e;
			} else
				throw new IllegalArgumentException("Stacks cannot store null elements.");
		top = startref.next;
	}

	@Override//Deque
	public void push(E e) {
		if (e == null)
			throw new IllegalArgumentException("Null elements are not supported.");
		e.next = top;
		top = e;
	}
	@Override//Deque
	public E peek() {
		return top;
	}
	@Override//Deque
	public E pop() {
		E e = top;
		if (e != null) {
			top = e.next;
			e.next = null;
		}
		return e;
	}

	public void clear() {
		top = null;
	}
	@Override//Collection
	public void forEach(Consumer<? super E> action) {
		for (E e=top; e!=null; e=e.next)
			action.accept(e);
	}
	@Override//Collection
	public int size() {
		int size=0;
		for (E e=top; e!=null; e=e.next)
			size++;
		return size;
	}


	protected class StackIterator implements Iterator<E> {
		protected E next, prev = null;
		protected StackIterator() {
			next = top;
		}
		public boolean hasNext() {
			return next != null;
		}
		public E next() {
			prev = next;
			E current = next;
			next = next.next;
			return current;
		}
		public void remove() {
			if (prev == null) {
				throw new IllegalStateException();
			} else if (prev != top) {
				E e = top;
				while (e.next != prev)
					e = e.next;
				e.next = next;
			} else
				top = top.next;
			prev = null;
		}
	}

	@Override//Iterable
	public Iterator<E> iterator() {
		return new StackIterator();
	}

	@Override//Collection
	public boolean add(E e) {
		push(e);
		return true;
	}
	@Override//Collection
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c)
			push(e);
		return true;
	}
	@Override//Collection
	public boolean contains(Object o) {
		for (E e=top; e!=null; e=e.next)
			if (o.equals(e))
				return true;
		return false;
	}
	@Override//Collection
	public boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (! contains(o))
				return false;
		return true;
	}
	@Override//Collection
	public boolean isEmpty() {
		return top == null;
	}


	@Override//List
	public E get(int index) throws IndexOutOfBoundsException {
		return getN(top, index);
	}
	@Override//List
	public void add(int index, E e) {
		Objects.requireNonNull(e, Stack.nullNotSupported);
		if (index == 0) {
			top = e;
			e.next = top;
		} else {
			E before = get(index-1);
			e.next = before.next;
			before.next = e;
		}
	}

	@Override//Deque
	public void addFirst(E e) {
		push(e);
	}
	@Override//Deque
	public void addLast(E arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override//Deque
	public Iterator<E> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override///Deque
	public E element() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override//Deque
	public E getFirst() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override//Deque
	public E getLast() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override//Deque
	public boolean offer(E arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override//Deque
	public boolean offerFirst(E arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override//Deque
	public boolean offerLast(E arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override//Deque
	public E peekFirst() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public E peekLast() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public E poll() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public E pollFirst() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public E pollLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override//Deque
	public E remove() {
		return removeFirst();
	}
	@Override//Deque
	public E removeFirst() {
		E first = top;
		if (first != null)
			top = first.next;
		return first;
	}
	@Override//Deque
	public boolean removeFirstOccurrence(Object o) {
		for (Stackable<E> e = new Stackable<>(top);  e.next != null;  e = e.next)
			if (o.equals(e.next)) {
				if (e.next == top)
					top = e.next.next;
				e.next = e.next.next;
				return true;
			}
		return false;
	}
	@Override//Deque
	public E removeLast() {
		if (top == null)
			return null;
		if (top.next == null) {
			top = null;
			return top;
		}
		E e = top;
		while (e.next.next != null)
			e = e.next;
		E last = e.next;
		e.next = null;
		return last;
	}
	@Override//Deque
	public boolean removeLastOccurrence(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
}
