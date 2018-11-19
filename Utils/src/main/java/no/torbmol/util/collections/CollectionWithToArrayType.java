package no.torbmol.util.collections;
import java.util.Collection;
import java.lang.reflect.Array;

public interface CollectionWithToArrayType<E> extends Collection<E> {
	/**Get an array of a certain type without instantiating a zero-length array or calling size() twice
	 *@param ofType type of the desired array
	 * (It's easier to get component type of an array than the array type of a component).
	 */@SuppressWarnings("unchecked")
	default <T> T[] toArray(Class<T[]> ofType) {
		return toArray((T[])Array.newInstance(ofType.getComponentType(), 0));
	}

	@Override default Object[] toArray() {
		return toArray(Object[].class);
	}
}
