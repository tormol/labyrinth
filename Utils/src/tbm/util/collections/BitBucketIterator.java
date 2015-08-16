package tbm.util.collections;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**A ListIterator for an empty unmodifiable List, except add(), which does nothing
 *Is not called BitBucketListIterator because it's not limited to Lists*/
public class BitBucketIterator<E> implements ListIterator<E> {
	public BitBucketIterator()
		{}

	@Override public boolean hasNext()    	{return false;}
	@Override public boolean hasPrevious()	{return false;}
	@Override public int nextIndex()      	{return  0;}
	@Override public int previousIndex()  	{return -1;}
	@Override public E next()             	{throw new NoSuchElementException();}
	@Override public E previous()         	{throw new NoSuchElementException();}
	@Override public void remove()        	{throw new IllegalStateException();}
	@Override public void set(E e)        	{throw new IllegalStateException();}
	@Override public void add(E e)        	{/*can add to an empty list*/}
}
