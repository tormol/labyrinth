package tbm.util.collections;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

//TODO javadoc
/**A Set that kinda works like a Map, the elements doesn't have to be unique, but they have a final field that has to.
 * (Useful if Map Values need to know their key / already has it as a field)
 * Cannot store nulls
 * Implementations cannot call someObject.equals(element) since classes using this feature breaks a.equals(b) == b.equals(a).
 *{@InheritDoc}*/
public interface SetWithGet<E> extends Set<E> {
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


	public static <E> SetWithGet<E> from(Set<E> existingSet) {
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
}
