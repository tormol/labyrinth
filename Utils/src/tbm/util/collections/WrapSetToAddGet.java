package tbm.util.collections;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**Wrap instances of an existing Set implementation to use a slow .get()
 * Should have been inside SetWithGet, but everything inside an interface is public*/
/*package*/ class WrapSetToAddGet<E> extends AbstractSet<E> implements SetWithGet<E>, Serializable {
	public final Set<E> wrapped;
	public WrapSetToAddGet(Set<E> set) {
		wrapped = Objects.requireNonNull(set);
	}
	@Override public Iterator<E> iterator()	{return wrapped.iterator();}
	@Override public int size()            	{return wrapped.size();}
	@Override public boolean add(E e)      	{return wrapped.add(e);}
	@Override public String toString()     	{return wrapped.toString();}
	@Override public int hashCode()        	{return wrapped.hashCode();}
	@Override public boolean equals(Object o) {
		if (o instanceof WrapSetToAddGet)
			return wrapped.equals( ((WrapSetToAddGet<?>) o).wrapped );
		return false;
	}
	private static final long serialVersionUID = 1L;
}
