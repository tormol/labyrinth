package tbm.util.collections;
import java.util.Objects;
import tbm.util.collections.randomAccessIterators.*;

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


	/**An unmodifiable array-backed ListIterator*/
	public static final class UnmodifiableArrayListIterator<E> extends UnmodifiableListIterator<E> {
		private final E[] array;
		@Override protected  int maxIndex()              	{return array.length;}
		@Override protected    E getIndex(int index)     	{return array[index];}

		public UnmodifiableArrayListIterator(E[] array, int start) {
			super(start);
			this.array = Objects.requireNonNull(array, "array is null");
		}
		@SafeVarargs//never changed
		public UnmodifiableArrayListIterator(E... array) {
			this(array, 0);
		}
	}


	/**An array-backed ListIterator that supports <tt>set(e)</tt> but not <tt>add(e)</tt> or <tt>remove()</tt>*/
	public static final class ArrayListIterator<E> extends ModifiableListIterator<E> {
		private final E[] array;
		@Override protected  int maxIndex()              	{return array.length;}
		@Override protected    E getIndex(int index)     	{return array[index];}
		@Override protected void setIndex(int index, E e)	{array[index] = e;}

		public ArrayListIterator(E[] array, int start) {
			super(start);
			this.array = Objects.requireNonNull(array, "array is null");
		}
		public ArrayListIterator(E[] array) {//E... would be unsafe
			this(array, 0);
		}

		/**@throws UnsupportedOperationException always
		 * @deprecated unsupported operation*/@Deprecated
		@Override public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}
