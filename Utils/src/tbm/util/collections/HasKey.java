package tbm.util.collections;

/**When you want to use SetWithGet
 * This interface adds requirements to some Object methods:
 * 	hashCode() to be the same as getKey().hashCode()
 * 	equals() must only use key for comparison, and should accept Keys in adittion to it's own class
 * You should extend AbstractHasKey if possible.*/
public interface HasKey<K> {
	/**Should never change nor return {@code null}*/
	K getKey();
}
