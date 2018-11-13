package tbm.util.collections;
import tbm.util.collections.randomAccessIterators.*;
import java.util.ListIterator;
import java.util.Objects;

/**randomAccessIterators for arrays*/
public class arrayIterators {
	/**An unmodifiable array-backed iterator*/
	public static final class UnmodifiableArrayIterator<E> extends UnmodifiableOneWayListIterator<E> {
		private final E[] array;
		@Override protected int maxIndex()              	{return array.length;}
		@Override protected   E getIndex(int index)     	{return array[index];}

		@SafeVarargs//never changed
		public UnmodifiableArrayIterator(E... array) {
			this.array = Objects.requireNonNull(array, "array is null");
		}
	}



	/***/
	public static class UnmodifiableSkipEmptyArrayIterator<E> extends UnmodifiableSkipEmpty<E> {
		private final E[] array;
		private final Object empty;
		@Override protected    int maxIndex()         	{return array.length;}
		@Override protected      E getIndex(int index)	{return array[index];}
		@Override protected Object emptyElement()     	{return empty;}

		@SafeVarargs//never changed
		public UnmodifiableSkipEmptyArrayIterator(Object empty, E... array) {
			this.empty = empty;
			this.array = Objects.requireNonNull(array, "array is null");
		}
	}



	/**An array-backed ListIterator that supports <tt>set(e)</tt> but not <tt>add(e)</tt> or <tt>remove()</tt>*/
	public static class ArrayListIterator<E> implements ListIterator<E> {
		private final E[] array;
		private int pos = -1;
		private boolean forward = true;

		public ArrayListIterator(E[] array) {//E... would be unsafe
			this.array = Objects.requireNonNull(array, "array is null");
		}
		public ArrayListIterator(E[] array, int start) {
			this(array);
			if (start < 0)
				throw new IndexOutOfBoundsException("pos = "+pos+" < 0");
			if (start > array.length)
				throw new IndexOutOfBoundsException("pos = "+pos+" > array.length = "+array.length);
			pos = start-1;
		}

		@Override public int nextIndex() {
			return pos + (forward ? 1 : 0);
		}
		@Override public int previousIndex() {
			return pos - (forward ? 0 : 1);
		}

		@Override public boolean hasNext() {
			return nextIndex() != array.length;
		}
		@Override public boolean hasPrevious() {
			return previousIndex() != -1;
		}

		@Override public E next() {try {
			E e = array[nextIndex()];
			pos = nextIndex();
			forward = true;
			return e;
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalStateException(ioobe); 
		}}
		@Override public E previous() {try {
			E e = array[previousIndex()];
			pos = previousIndex();
			forward = false;
			return e;
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalStateException(ioobe); 
		}}

		@Override public void set(E e) {try {
			array[pos] = e;
		} catch (ArrayIndexOutOfBoundsException ioobe) {
			throw new IllegalStateException();
		}}

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public void add(E e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}



	/**An unmodifiable array-backed ListIterator*/
	public static class UnmodifiableArrayListIterator<E> extends ArrayListIterator<E> {
		@SafeVarargs//never changed
		public UnmodifiableArrayListIterator(int start, E... array) {
			super(array, start);
		}
		@SafeVarargs//never changed
		public UnmodifiableArrayListIterator(E... array) {
			super(array);
		}

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public final void set(E e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}
