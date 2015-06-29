package tbm.util.collections;
import java.util.Set;

/**A Set that kinda works like a Map, the elements doesn't have to be unique, but they have a final field that has to.
 * (Useful if Map Values need to know their key / already has it as a field.)
 * Cannot store nulls
 *{@InheritDoc}
 *@param <K> key
 *@param <E> element
 *///TODO: add .keySet() or .keyIterator()?
public interface IndexedSet<K, E extends HasKey<K>> extends Set<E> {
	/**Analogous to {@See Map.get()}*/
	E get(K key);
}
