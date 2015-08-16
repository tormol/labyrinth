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
		protected int next = 0;
		protected Unmodifiable(E[] array) {
			this.array = Objects.requireNonNull(array);
		}

		/**Allows using this class with foreach loops directly.
		 *@return {@code this}*/@Override//Iterable
		public Iterator<E> iterator() {
			return this;
		}

		@Override public boolean hasNext() {
			return next != array.length;
		}
		public final int nextIndex() {
			hasNext();
			return next;
		}
		@Override public final E next() {
			if (hasNext())
				return array[next++];
			throw new NoSuchElementException();
		}

		/**Return the next element but don't advance the iteration.*/
		public final E peek() {
			return hasNext() ? array[next] : null;//adds after return
		}
		/**Skip the next element.*/
		public final void skip() {
			if (hasNext())
				next++;
		}
	}



	/**{@inheritDoc}
	 *Also skips references to instance.
	 *Useful for some collections.*/
	public static class UnmodifiableSkipEmpty<E extends T, T> extends Unmodifiable<E> {
		protected final T empty;//protected to not leak poison values
		public UnmodifiableSkipEmpty(E[] array, T empty) {
			super(array);
			this.empty = empty;
		}

		/**{@inheritDoc}
		 *Automatically skip null elements.*/
		@Override public boolean hasNext() {
			while (next < array.length)
				if (array[next] == empty)
					next++;
				else
					return true;
			return false;
		}

	}


	/**{@inheritDoc}
	 *Supports <tt>remove()</tt>.*/
	public static class SkipEmpty<E extends T, T> extends UnmodifiableSkipEmpty<E, T> {
		public SkipEmpty(E[] array, T empty) {
			super(array, empty);
		}

		protected void replaceLast(T t) {
			if (next == 0)
				throw new IllegalStateException();
			((T[])array)[next] = t;//why does this work?
		}

		/**replaces the last returned element with e*/
		public final void replace(E e) {
			replaceLast(e);
		}

		/**{@inheritDoc}
		 *replaces the last returned element with empty*/
		@Override public final void remove() {
			replaceLast(empty);
		}

		//add() could work when there are null elements before nextIndex(), but that would be unreliable
	}
}
