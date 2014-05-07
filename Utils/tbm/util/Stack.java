package tbm.util;
import java.security.InvalidParameterException;
import java.lang.Iterable;
import java.util.Iterator;
import java.util.Collection;
@SuppressWarnings("unchecked")//(E)Stackable<E>
public class Stack<E extends Stack.Stackable<E>> implements Iterable<E>, Iterator<E>, Collection<E> {
	public static class Stackable<E extends Stack.Stackable<E>> {
		E next;
	}
	private E top;
	public Stack() {
		top = null;
	}
	public Stack(E e) {
		top = e;
	}
	public Stack(Stack<E> s) {
		top = s.top;
	}
	public E push(E e) {
		if (e == null)
			throw new InvalidParameterException("Null elements are not supported.");
		e.next = top;
		top = e;
		return top;
	}

	public E peek() {
		return top;
	}
	public E pop() {
		E e = (E)top;
		if (top != null)
			top = top.next;
		e.next = null;
		return e;
	}
	public void skip() {
		if (top != null)
			top = top.next;
	}

	public void clear() {
		top = null;
	}
	public int size() {
		int size=0;
		for (E e=top; e!=null; e=e.next)
			size++;
		return size;
	}

	@Override//Iterator
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return (top != null);
	}

	@Override//Iterator
	public E next() {
		return pop();
	}

	@Override//Iterator
	/**Not supported
	 *@throws UnsupportedOperationException always*/
	//Supporting remove() would require extra variables
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override//Iterable
	public Iterator<E> iterator() {
		return new Stack<E>(this);
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
		for (E e : this)
			if (e==o)
				return true;
		return false;
	}
	@Override//Collection
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			boolean contains = false;
			for (E e : this)
				if (o==e)
					contains = true;
			if (!contains)
				return false;
		}
		return true;
	}
	@Override//Collection
	public boolean isEmpty() {
		return (top == null);
	}
	@Override//Collection
	public boolean remove(Object o) {
		E prev = null;
		for (E e : this) {
			if (o==e) {
				if (prev==null)
					top = null;
				else
					prev.next = e.next;
				return true;
			}
			prev = e;
		}
		return false;
	}
	@Override//Collection
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override//Collection
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override//Collection
	public Object[] toArray() {
		Object[] a = new Object[size()];
		int i=0;
		for (E e : this) {
			a[i] = e;
			i++;
		}
		return a;
	}
	@Override//Collection
	public <T> T[] toArray(T[] a) {
		int i=0;
		for (E e : this) {
			if (i==a.length) {
				int size = a.length + new Stack<E>(e).size();
				T[] tmp = (T[])new Object[size];
				System.arraycopy(a, 0, tmp, 0, a.length);
				a = tmp;
			}
			a[i] = (T)e;
			i++;
		}
		for (; i<a.length; i++)
			a[i] = null;
		return a;
	}
}
