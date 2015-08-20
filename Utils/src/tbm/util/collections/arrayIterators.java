package tbm.util.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**If you need an Iterator for an array-backed Collection*/
public final class arrayIterators {
	/**It's hard And I don't need it
	  *If you for some reason really need an Listiterator, there was a nealy complete one in ArrayIterator before 2015*/
	protected static final int attemts_at_implementing_ListIterator = 2;

	/**An Iterator for arrays that doesn't support remove(),
	 *  but have <tt>skip()</tt>, <tt>peek()</tt> and <tt>nextIndex()</tt>
	 *  from ListIterator.*/
	public static class Unmodifiable<E> implements Iterator<E>, Iterable<E> {
		protected final E[] array;
		/**position of the last element returned by next, starts at -1 and can be larger than array.length*/
		protected int pos = -1;
		protected Unmodifiable(E[] array) {
			this.array = Objects.requireNonNull(array);
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
		@Override public final boolean hasNext() {
			return nextIndex() < array.length;
		}
		@Override public E next() {
			try {
				return array[pos = nextIndex()];
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		/**Return the next element but don't advance the iteration
		 *@throws NoSuchElementException if there are no more elements.
		 * (returning null would be meaningless if there can be null elements in the array,
		 * and {@code if (hasNext() && peek().someFunc())} is clearer than {@code if (peek() != null  &&  peek().somefunc())}
		 */
		public final E peek() {
			try {
				return array[nextIndex()];
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}
		/**Skip the next element.*/
		public final void skip() {
			pos = nextIndex();
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

		/**@return {@code null}*/
		protected Object emptyElement() {
			return null;
		}
	}


	/**{@inheritDoc}
	 *Supports <tt>remove()</tt>.*/
	public static class SkipEmpty<E> extends UnmodifiableSkipEmpty<E> {
		public SkipEmpty(E[] array) {
			super(array);
		}

		/**@return {@code true} if element was empty*/
		protected void setLast(Object o) {
			try {
				if (((Object[])array)[pos] == emptyElement())
					throw new IllegalStateException("Element has already been removed");
				((Object[])array)[pos] = o;
			} catch (ArrayIndexOutOfBoundsException e) {
				if (pos == -1)
					throw new IllegalStateException("Must call next() first");
				if (pos >= array.length)
					throw new IllegalStateException("No more elements");
				throw e;
			}
		}

		/**replaces the last returned element with e*/
		public final void set(E e) {
			setLast(e);
		}

		/**{@inheritDoc}
		 *replaces the last returned element with empty*/
		@Override public final void remove() {
			setLast(emptyElement());
			//FIXME somehow decrement size
		}

		//add() could work when there are null elements before nextIndex(), but that would be unreliable
	}
}
