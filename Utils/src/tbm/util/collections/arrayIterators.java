package tbm.util.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**If you need an Iterator for an unmodifiable array-backed Collection*/
public final class arrayIterators {
	/**An Iterator for arrays that doesn't support remove() but has every function from ListIterator except previous()	 *  
	 *  from ListIterator.*/
	public static class Unmodifiable<E> implements Iterator<E>, Iterable<E> {
		protected final E[] array;
		/**position of the last element returned by next, starts at -1 and can be larger than array.length*/
		protected int pos = -1;
		protected Unmodifiable(E[] array) {
			this.array = Objects.requireNonNull(array, "array is null");
		}

		/**Allows using this class with foreach loops directly.
		 *@return {@code this}*/@Override//Iterable
		public Iterator<E> iterator() {
			return this;
		}

		//If I override forEach or forEachRemaining, skipEmpty would have to override again

		/**return the next index to be returned, or >= array.length if at the end*/
		public int nextIndex() {
			return pos+1;
		}
		/**return index of the last element returned by next()*/
		public int previousIndex() {
			return pos;
		}

		@Override public final boolean hasNext() {
			return nextIndex() < array.length;
		}
		/**@return true if <tt>next()</tt> has been called*/
		public final boolean hasPrevious() {
			return previousIndex() >= 0;
		}

		/**get the element at pos
		 *@param update_pos should {@code this.pos} be set to pos if pos is valid?
		 *@throws NoSuchElementException if pos is an invalid index*/
		protected E get(int pos, boolean update_pos) {
			try {
				E e = array[pos];//fail before changing position or direction
				if (update_pos)
					this.pos = pos;
				return e;
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		/**{@inheritDoc}
		 *Is idempotent when NoSuchElementException is thrown*/
		@Override public E next() {
			return get(nextIndex(), true);
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

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public final void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}


	public static class ListIterator<E> extends Unmodifiable<E> implements java.util.ListIterator<E> {
		protected boolean forward = true;
		protected ListIterator(E[] array, int pos) throws IndexOutOfBoundsException {
			super(array);
			this.pos = pos;
			if (pos < 0)
				throw new IndexOutOfBoundsException("pos = "+pos+" < 0");
			if (pos > array.length)
				throw new IndexOutOfBoundsException("pos = "+pos+" > array.length = "+array.length);
		}

		@Override public int nextIndex() {
			return pos + (forward ? 1 : 0);
		}
		@Override public int previousIndex() {
			return pos - (forward ? 0 : 1);
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

		@Override public void set(E e) {
			try {
				array[pos] = e;
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				throw new IllegalStateException();
			}
		}

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public final void add(E arg0) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}


	public static class UnmodifiableListIterator<E> extends ListIterator<E> {
		public UnmodifiableListIterator(E[] array, int start) {
			super(array, start);
		}
		@SuppressWarnings("unchecked")
		public UnmodifiableListIterator(E... array) {
			this(array, 0);
		}

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public final void set(E arg0) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	
	/**{@inheritDoc}
	 *Also skips references to instance.
	 *Useful for some collections.*/
	public static class UnmodifiableSkipEmpty<E> extends Unmodifiable<E> {
		/**cache for nextIndex()*/
		protected int next = -1;

		public UnmodifiableSkipEmpty(E[] array) {
			super(array);
		}

		@Override public int nextIndex() {
			if (next == -1)
				for (next = pos+1;  next < array.length;  next++)
					if (array[next] != emptyElement())
						break;
			return next;
		}

		@Override public E next() {
			E e = super.next();
			next = -1;//reset if next() didn't throw
			return e;
		}

		/**@return instance that signifies an unused slot.
		 *Default is {@code null}*/
		protected Object emptyElement() {
			return null;
		}
	}
}
