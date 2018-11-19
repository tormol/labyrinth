package no.torbmol.util.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**ListIterator without previous() or *index()*/
public interface ExtendedIterator<E> extends Iterator<E> {
	/**is true if <tt>next()</tt> has been called*/
	boolean hasPrevious();

	/**get the next element that will be returned by <tt>next()</tt> without advancing the iterator
	 *@throws NoSuchElementException if there are no more elements
	 * (null migth be a valid value, and {@code if (hasNext() && peek().someFunc())}
	 *	 is clearer than {@code if (peek() != null  &&  peek().somefunc())}
	 */
	E peekNext() throws NoSuchElementException;
	/**get the last element returned by <tt>next()</tt>
	 *@throws NoSuchElementException if there is no previous element
	 *@throws IllegalStateException optionally if the previous element has been removed*/
	E peekPrevious() throws NoSuchElementException, IllegalStateException;

	/**replace the last element*/
	void set(E e) throws IllegalStateException, UnsupportedOperationException;

	//*index() and positional add() only makes sense for lists
}
