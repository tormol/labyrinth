package tbm.util.collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**Wrap instances of an existing Set implementation to use a slow .get()
 * Should have been inside SetWithGet, but everything inside an interface is public*/
public class WrapSetToAddGet<E> implements SetWithGet<E> {
	protected final Set<E> wrapped;
	public WrapSetToAddGet(Set<E> set) {
		wrapped = Objects.requireNonNull(set);
	}

	@Override public           int            size()                            {return wrapped.size();}
	@Override public       boolean         isEmpty()                            {return wrapped.isEmpty();}
	@Override public       boolean        contains(Object o)                    {return wrapped.contains(o);}
	@Override public       boolean     containsAll(Collection<?> c)             {return wrapped.containsAll(c);}
	@Override public       boolean             add(E e)                         {return wrapped.add(e);}
	@Override public       boolean          addAll(Collection<? extends E> c)   {return wrapped.addAll(c);}
	@Override public       boolean          remove(Object o)                    {return wrapped.remove(o);}
	@Override public       boolean       removeAll(Collection<?> c)             {return wrapped.removeAll(c);}
	@Override public       boolean       retainAll(Collection<?> c)             {return wrapped.retainAll(c);}
	@Override public          void           clear()                            {       wrapped.clear();}
	@Override public       boolean        removeIf(Predicate<? super E> filter) {return wrapped.removeIf(filter);}
	@Override public        String        toString()                            {return wrapped.toString();}
	@Override public           int        hashCode()                            {return wrapped.hashCode();}
	@Override public       boolean          equals(Object o)                    {return wrapped.equals(o);}
	@Override public       Object[]        toArray()                            {return wrapped.toArray();}
	@Override public <T>        T[]        toArray(T[] a)                       {return wrapped.toArray(a);}
	@Override public Spliterator<E>    spliterator()                            {return wrapped.spliterator();}
	@Override public      Stream<E>         stream()                            {return wrapped.stream();}
	@Override public      Stream<E> parallelStream()                            {return wrapped.parallelStream();}
	@Override public    Iterator<E>       iterator()                            {return wrapped.iterator();}
	@Override public          void         forEach(Consumer<? super E> action)  {       wrapped.forEach(action);}
}