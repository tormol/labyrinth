package tbm.util.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

//TODO javadoc
/**Iterators for collections that have random access (are array-backed)*/
public final class randomAccessIterators {
	private static abstract class OneWayListIterator<E> implements Iterator<E>, Iterable<E> {
		protected abstract E getIndex(int index);
		protected abstract int maxIndex();

		/**position of the last element returned by next, starts at -1 and can be larger than array.length*/
		protected int pos = -1;

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
			return nextIndex() < maxIndex();
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
				E e = getIndex(pos);//fail before changing position or direction
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
	}


	/**An Iterator for arrays that doesn't support remove() but has every function from ListIterator except previous()	 *  
	 *  from ListIterator.*/
	public static interface Unmodifiable<E> extends Iterator<E> {
		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override default void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}


	public static interface Modifiable<E> extends Iterator<E> {
		default E emptyValue() {
			return null;
		}

		@Override void remove();
	}




	public static abstract class UnmodifiableOneWayListIterator<E> extends OneWayListIterator<E> implements Unmodifiable<E> {
		
	}

	public static abstract class ModifiableOneWayListIterator<E> extends OneWayListIterator<E> implements Modifiable<E> {
		
	}




	private static abstract class ListIterator<E> extends OneWayListIterator<E> implements java.util.ListIterator<E> {
		protected boolean forward = true;
		protected ListIterator(int pos) throws IndexOutOfBoundsException {
			this.pos = pos;
			if (pos < 0)
				throw new IndexOutOfBoundsException("pos = "+pos+" < 0");
			if (pos > maxIndex())
				throw new IndexOutOfBoundsException("pos = "+pos+" > array.length = "+maxIndex());
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

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public final void add(E e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}


	public static abstract class ModifiableListIterator<E> extends ListIterator<E> implements Modifiable<E> {
		protected ModifiableListIterator(int pos) throws IndexOutOfBoundsException {
			super(pos);
		}

		protected abstract void setIndex(int index, E value);
		@Override public void set(E e) {
			try {
				setIndex(pos, e);
			} catch (IndexOutOfBoundsException ioobe) {
				throw new IllegalStateException();
			}
		}

		@Override public void remove() {
			try {
				if (getIndex(pos) == emptyValue())
					throw new IllegalStateException("already removed");
				setIndex(pos, emptyValue());
			} catch (IndexOutOfBoundsException ioobe) {
				throw new IllegalStateException();
			}
		}
	}


	public static abstract class UnmodifiableListIterator<E> extends ListIterator<E> implements Unmodifiable<E> {
		protected UnmodifiableListIterator(int pos) throws IndexOutOfBoundsException {
			super(pos);
		}

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public final void set(E e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}




	/**{@inheritDoc}
	 *Also skips references to instance.
	 *Useful for some collections.*/
	private static abstract class SkipEmpty<E> extends OneWayListIterator<E> {
		/**cache for nextIndex()*/
		protected int next = -1;

		@Override public int nextIndex() {
			if (next == -1)
				for (next = pos+1;  next < maxIndex();  next++)
					if (getIndex(next) != emptyElement())
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


	public static abstract class UnmodifiableSkipEmpty<E> extends SkipEmpty<E> implements Unmodifiable<E> {
		
	}


	public static abstract class ModifiableSkipEmpty<E> extends SkipEmpty<E> implements Modifiable<E> {
		protected abstract void setIndex(int index, E e);
		protected abstract void delIndex(int index);

		/**replaces the last returned element with e*/
		public final void set(E e) {
			try {
				if (getIndex(pos) == emptyElement())
					throw new IllegalStateException("Element has already been removed");
				if (e == emptyValue())
					delIndex(pos);
				setIndex(pos, e);
			} catch (IndexOutOfBoundsException ioobe) {
				if (pos == -1)
					throw new IllegalStateException("Must call next() first");
				if (pos >= maxIndex())
					throw new IllegalStateException("No more elements");
				throw ioobe;
			}	
		}

		/**{@inheritDoc}
		 *replaces the last returned element with empty*/
		public final void remove() {
			set(emptyValue());
		}
	}
}
