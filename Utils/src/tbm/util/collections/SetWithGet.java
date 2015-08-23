package tbm.util.collections;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

//TODO javadoc
/**A Set that kinda works like a Map, the elements doesn't have to be unique, but they have a final field that has to.
 * (Useful if Map Values need to know their key / already has it as a field)
 * Cannot store nulls
 * Implementations cannot call someObject.equals(element) since classes using this feature breaks a.equals(b) == b.equals(a).
 *{@InheritDoc}*/
public interface SetWithGet<E> extends Set<E>, CollectionWithToArrayType<E> {
	/**Analogous to {@See Map.get()}
	 * Similar to contains(), but returns the existing element
	 * Default implementation iterates through every element.
	 *///by taking an Object and not a Key, implementations are not limited to subclasses of HasKey
	default E get(Object o) {
		for (E e : this)
			if (e.equals(o))
				return e;
		Objects.requireNonNull(o);
		return null;
	}

	@Override//Set and CollectionWithToArrayType, so the compile cant choose the default method in the same package
	default Object[] toArray() {
		return CollectionWithToArrayType.super.toArray();
	}


	public static <E, T extends Set<E> & Serializable> SetWithGet<E> from(T existingSet) {
		return new WrapSetToAddGet<>(existingSet);
	}

	/**{@InheritDoc}
	 *A placeholder until i'm sure IndexedLeanHashSet is bug-free
	 *Include this instead of java.util's*/
	public static final class HashSet<E> extends java.util.HashSet<E> implements SetWithGet<E> {
		public HashSet(int intialCapacity, float loadFactor)	{super(intialCapacity, loadFactor);}
		public HashSet(int intialCapacity)                  	{super(intialCapacity);}
		public HashSet(Collection<? extends E> c)           	{super(c);}
		public HashSet()                                    	{super();}
		private static final long serialVersionUID = 1L;
	}


	/**Wrap instances of an existing Set implementation to use a slow .get()*/
	public static class WrapSetToAddGet<E> implements SetWithGet<E>, CollectionWithToArrayType<E>, Serializable {
		protected final Set<E> wrapped;
		/**@param set if it's a HashSet, you should use */
		protected <T extends Set<E> & Serializable> WrapSetToAddGet(T set) {
			wrapped = Objects.requireNonNull(set);
		}

		@Override public final           int            size()                            {return wrapped.size();          }
		@Override public final       boolean         isEmpty()                            {return wrapped.isEmpty();       }
		@Override public final       boolean        contains(Object o)                    {return wrapped.contains(o);     }
		@Override public final       boolean     containsAll(Collection<?> c)             {return wrapped.containsAll(c);  }
		@Override public final       boolean             add(E e)                         {return wrapped.add(e);          }
		@Override public final       boolean          addAll(Collection<? extends E> c)   {return wrapped.addAll(c);       }
		@Override public final       boolean          remove(Object o)                    {return wrapped.remove(o);       }
		@Override public final       boolean       removeAll(Collection<?> c)             {return wrapped.removeAll(c);    }
		@Override public final       boolean       retainAll(Collection<?> c)             {return wrapped.retainAll(c);    }
		@Override public final          void           clear()                            {       wrapped.clear();         }
		@Override public final       boolean        removeIf(Predicate<? super E> filter) {return wrapped.removeIf(filter);}
		@Override public final        String        toString()                            {return wrapped.toString();      }
		@Override public final           int        hashCode()                            {return wrapped.hashCode();      }
		@Override public final       boolean          equals(Object o)                    {return wrapped.equals(o);       }
		@Override public final       Object[]        toArray()                            {return wrapped.toArray();       }
		@Override public final <T>        T[]        toArray(T[] a)                       {return wrapped.toArray(a);      }
		@Override public final Spliterator<E>    spliterator()                            {return wrapped.spliterator();   }
		@Override public final      Stream<E>         stream()                            {return wrapped.stream();        }
		@Override public final      Stream<E> parallelStream()                            {return wrapped.parallelStream();}
		@Override public final    Iterator<E>       iterator()                            {return wrapped.iterator();      }
		@Override public final          void         forEach(Consumer<? super E> action)  {       wrapped.forEach(action); }

		private static final long serialVersionUID = 1;
	}
}
