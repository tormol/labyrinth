package no.torbmol.util.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

//TODO javadoc
/**Iterators for collections that have random access (are array-backed)*/
public final class randomAccessIterators {
	private static abstract class OneWayListIterator<E> implements ExtendedIterator<E>, Iterable<E> {
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
		protected int nextIndex() {
			return pos+1;
		}
		/**@return {@code this.pos}*/
		protected int previousIndex() {
			return pos;
		}

		@Override public final boolean hasNext() {
			return nextIndex() < maxIndex();
		}
		@Override public final boolean hasPrevious() {
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

		@Override public final E peekNext() {
			return get(nextIndex(), false);
		}
		@Override public final E peekPrevious() {
			return get(previousIndex(), false);
		}
	}


	/**An Iterator for arrays that doesn't support remove() but has every function from ListIterator except previous()
	 *  from ListIterator.*/
	public static interface Unmodifiable<E> extends Iterator<E> {
		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override default void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		default void set(E e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}


	public static interface Modifiable<E> extends Iterator<E> {
		@Override void remove();
		//cannot have lastIndex since interface methods must be public
	}




	public static abstract class UnmodifiableOneWayListIterator<E> extends OneWayListIterator<E> implements Unmodifiable<E> {
		@Override public final void set(E element) {
			Unmodifiable.super.set(element);
		}
	}

	public static abstract class ModifiableOneWayListIterator<E> extends OneWayListIterator<E> implements Modifiable<E>
		{}


	/**{@inheritDoc}
	 *Also skips references to instance.
	 *Useful for some collections.*/
	private static abstract class SkipEmpty<E> extends OneWayListIterator<E> {
		/**cache for nextIndex()*/
		protected int next = -1;

		@Override protected int nextIndex() {
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

		/**@return an instance that signifies an unused slot.
		 *Default is {@code null}*/
		protected Object emptyElement() {
			return null;
		}
	}


	public static abstract class UnmodifiableSkipEmpty<E> extends SkipEmpty<E> implements Unmodifiable<E> {
		@Override public final void set(E element) {
			Unmodifiable.super.set(element);
		}
	}


	public static abstract class ModifiableSkipEmpty<E> extends SkipEmpty<E> implements Modifiable<E> {
		protected abstract void setIndex(int index, E e);
		protected abstract void removeIndex(int index);

		/**pos validation for set() and remove()*/
		protected int lastIndex() {
			if (pos < 0)
				throw new IllegalStateException("call next() first");
			if (getIndex(pos) == emptyElement())
				throw new IllegalStateException("element has been removed");
			return pos;
		}

		/**replaces the last returned element with e*/
		public final void set(E e) {
			setIndex(lastIndex(), e);
		}
		@Override public final void remove() {
			removeIndex(lastIndex());
		}
	}
}
