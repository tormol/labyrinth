package tbm.util.collections;
import java.util.Objects;

//TODO javadoc
/**When you want to use SetWithGet
 * This interface adds requirements to some Object methods:
 * 	hashCode() to be the same as getKey().hashCode()
 * 	equals() must only use key for comparison, and should accept Keys in adittion to it's own class
 * You should extend AbstractHasKey if possible.*/
public interface HasKey<K> {
	/**Should never change nor return {@code null}*/
	K getKey();



	/**implements hashCode() and equals() correctly*/
	public abstract class AbstractHasKey<K> implements HasKey<K> {
		/***/@Override//Object
		public final boolean equals(Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (o.getClass() == this.getClass())
				return getKey().equals(((AbstractHasKey<?>)o).getKey());
			if (getKey().getClass().isAssignableFrom(o.getClass()))
				return getKey().equals(o);
			return false;
		}
	
		/**@return {@code getKey().hashCode()}*/@Override//Object
		public final int hashCode() {
			return getKey().hashCode();
		}
	
		/**@return {@code getKey().toString()}*/@Override//Object
		public String toString() {
			return getKey().toString();
		}
	}



	public static class CompleteHasKey<K> extends AbstractHasKey<K> {
		protected final K key;
		protected CompleteHasKey(K key) {
			this.key = Objects.requireNonNull(key);
		}
		@Override public final K getKey() {
			return key;
		}
	}
}
