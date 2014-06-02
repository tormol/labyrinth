package tbm.util;
import java.util.Iterator;

@SuppressWarnings("unchecked")//(E)Stackable<E>
public class QuickList<E extends QuickList.Single> extends java.util.AbstractQueue<E> implements java.util.Queue<E> {
	public static class Single {
		protected Single next;
	}

	Single start = null;
	Single end = null;

	@Override
	public boolean add(E e) {
		e.next = null;
		if (end != null)
			end.next = e;
		else
			start = e;
		end = e;
		return true;
	}


	@Override
	public int size() {
		int size=0;
		for (Single e = start;  e != null;  e = e.next)
			size++;
		return size;
	}


	@Override
	public boolean offer(E e) {
		return false;
	}


	@Override
	public E peek() {
		return (E)start;
	}


	@Override
	public E poll() {
		if (start == null)
			return null;
		E e = (E)start;
		start = start.next;
		if (start == null)
			end = null;
		return e;
	}


	@Override
	public Iterator<E> iterator() {
		final QuickList<E> denne = this;
		return new Iterator<E>(){
			Single top = denne.start;
			Single prev = null;
			@Override
			public boolean hasNext() {
				return (top != null);
			}

			@Override
			public E next() {
				prev = top;
				top = top.next;
				return (E)prev;
			}

			@Override
			public void remove() {
				if (prev==null)
					throw new IllegalStateException("No element to remove");
				denne.remove(prev);
				prev = null;
			}
		};
	}
}
