package tbm.util.collections;
import java.util.Iterator;
import java.util.Objects;

/**If you need an iterator for an array-backed Collection,
 *or are too lazy to write <tt>for (int i=0; i<array.length; i++)</tt>*/
public final class ArrayIterators {
	/**@return new ArrayIterators.Unmodifiable<E>(array);*/
	public static <E> Unmodifiable<E> unmodifiable(
			@SuppressWarnings("unchecked") E... array
		) {
		return new Unmodifiable<E>(array);
	}

	/**An Iterator for arrays that doesn't support remove(),
	 *  but have <tt>skip()</tt>, <tt>peek()</tt> and <tt>nextIndex()</tt>
	 *  from ListIterator.*/
	public static class Unmodifiable<E> implements Iterator<E>, Iterable<E> {
		protected final Object[] array;
		protected int next = 0;//last
		public Unmodifiable(E[] array) {
			this(array, false);
		}
		protected Unmodifiable(Object[] array, boolean just_need_a_different_signature) {
			this.array = Objects.requireNonNull(array);
		}

		@Override//Iterator
		public boolean hasNext() {
			return next != array.length;
		}
		/**{@inheritDoc}
		 *If you override this you should also override peek();
		 */@Override//Iterator
		public E next() {
			return hasNext() ? (E)array[next++] : null;//adds after return
		}

		/**Allows using this class with foreach loops.
		 *@return this*/@Override//Iterable
		public Iterator<E> iterator() {
			return this;
		}

		//Three easy methods from ListIterator
		/**Return the index of the next element to be returned,
		 * or array.length if at the end.
		 *@see ListIterator.nextIndex()*/
		public int nextIndex() {
			hasNext();
			return next;
		}
		/**Return the next element but don't advance the iteration.
		 *@see ListIterator.nextIndex()*/
		public E peek() {
			return hasNext() ? (E)array[next] : null;//adds after return
		}
		/**Skip the next element.
		 *@see ListIterator.nextIndex()*/
		public void skip() {
			if (hasNext())
				next++;
		}
	}


	/**{@inheritDoc}
	 *Also skips null elements.
	 *Useful for some collections.
	 */
	public static class UnmodifiableSkipNulls<E> extends Unmodifiable<E> {
		public UnmodifiableSkipNulls(E[] array) {
			super(array);
		}

		/**{@inheritDoc}
		 *Automatically skip null slots.*/@Override
		public boolean hasNext() {
			while (next < array.length)
				if (array[next] == null)
					next++;
				else
					return true;
			return false;
		}
	}


	/**{@inheritDoc}
	 *Supports <tt>remove()</tt>.
	 */
	public static class SkipNulls<E> extends UnmodifiableSkipNulls<E> {
		public SkipNulls(E[] array) {
			super(array);
		}

		/**{@inheritDoc}
		 *Automatically skip null slots.*/@Override
		public void remove() {
			if (next == 0  ||  array[next-1] == null)
				throw new IllegalStateException();
			array[next-1] = null;
		}
	}


	/**{@inheritDoc}
	 *Also skips references to instance.
	 *Useful for some collections.
	 */
	public static class UnmodifiableSkipRefs<E> extends Unmodifiable<E> {
		protected final Object skip;
		public UnmodifiableSkipRefs(Object[] array, Object skip) {
			super(array, true);
			this.skip = skip;
		}

		/**{@inheritDoc}
		 *Automatically skip null slots.*/@Override
		public boolean hasNext() {
			while (next < array.length)
				if (array[next] == skip)
					next++;
				else
					return true;
			return false;
		}
	}


	/**{@inheritDoc}
	 *Supports <tt>remove()</tt>.
	 */
	public static class SkipEmpty<E> extends UnmodifiableSkipRefs<E> {
		public SkipEmpty(Object[] array, Object empty) {
			super(array, empty);
		}

		/**{@inheritDoc}
		 *Automatically skip null slots.*/@Override
		public void remove() {
			if (next == 0  ||  array[next-1] == skip)
				throw new IllegalStateException();
			array[next-1] = skip;
		}
	}


	//If you for some reason really need an Listiterator;
	//Revisions before 2015 had a nearly complete implementation
	//in ArrayIterator. (without the plural s).
}
