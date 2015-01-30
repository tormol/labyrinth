package tbm.util.collections;

import java.util.*;

/**Mimics /dev/null: add/push are accepted, but does nothing, otherwize acts as an empty collection*/
public class BitBucket<E> implements Collection<E>, List<E>, Set<E>, Queue<E>, Deque<E> {
	@Override//Collections
	public boolean add(E e) {
		return false;
	}

	@Override//Collections
	public boolean addAll(Collection<? extends E> c) {
		return false;
	}

	@Override//Collections
	public void clear()
		{}

	@Override//Collections
	public boolean contains(Object o) {
		return false;
	}

	@Override//Collections
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override//Collections
	public boolean isEmpty() {
		return true;
	}

	@Override//Collections
	public ListIterator<E> iterator() {
		return new BitBucketIterator<E>();
	}

	@Override//Collections
	public boolean remove(Object o) {
		return false;
	}

	@Override//Collections
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override//Collections
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override//Collections
	public int size() {
		return 0;
	}

	@Override//Collections
	public Object[] toArray() {
		return new Object[0];
	}

	@SuppressWarnings("unchecked")
	@Override//Collections
	public <T> T[] toArray(T[] a) {
		if (a==null)
			return (T[])toArray();
		for (int i=0; i<a.length; i++)
			a[i] = null;
		return a;
	}


	
	@Override//List
	public void add(int i, E arg1)
		{}

	@Override//List
	public boolean addAll(int arg0, Collection<? extends E> arg1) {
		return false;
	}

	@Override//List
	public E get(int i) {
		throw new IndexOutOfBoundsException();
	}

	@Override//List
	public int indexOf(Object arg0) {
		return -1;
	}

	@Override//List
	public int lastIndexOf(Object arg0) {
		return -1;
	}

	@Override//List
	public ListIterator<E> listIterator() {
		return new BitBucketIterator<E>();
	}

	@Override//List
	public ListIterator<E> listIterator(int arg0) {
		return new BitBucketIterator<E>();
	}

	@Override//List
	public E remove(int arg0) {
		return null;
	}

	@Override//List
	public E set(int arg0, E arg1) {
		return null;
	}

	@Override//List
	public List<E> subList(int arg0, int arg1) {
		return null;
	}

	@Override//List//Set
	//remove if java < 8
	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(this, 0);
	}



	@Override//Queue
	public E element() {
		return null;
	}

	@Override//Queue
	public boolean offer(E arg0) {
		return false;
	}

	@Override//Queue
	public E peek() {
		return null;
	}

	@Override//Queue
	public E poll() {
		return null;
	}

	@Override//Queue
	public E remove() {
		throw new NoSuchElementException();
	}



	@Override//Deque
	public void addFirst(E arg0)
		{}

	@Override//Deque
	public void addLast(E arg0)
		{}

	@Override//Deque
	public Iterator<E> descendingIterator() {
		return new BitBucketIterator<>();
	}

	@Override//Deque
	public E getFirst() {
		return null;
	}

	@Override//Deque
	public E getLast() {
		return null;
	}

	@Override//Deque
	public boolean offerFirst(E arg0) {
		return false;
	}

	@Override//Deque
	public boolean offerLast(E arg0) {
		return false;
	}

	@Override//Deque
	public E peekFirst() {
		return null;
	}

	@Override//Deque
	public E peekLast() {
		return null;
	}

	@Override//Deque
	public E pollFirst() {
		return null;
	}

	@Override//Deque
	public E pollLast() {
		return null;
	}

	@Override//Deque
	public E pop() {
		return null;
	}

	@Override//Deque
	public void push(E arg0)
		{}

	@Override//Deque
	public E removeFirst() {
		return null;
	}

	@Override//Deque
	public boolean removeFirstOccurrence(Object arg0) {
		return false;
	}

	@Override//Deque
	public E removeLast() {
		return null;
	}

	@Override//Deque
	public boolean removeLastOccurrence(Object arg0) {
		return false;
	}
}
