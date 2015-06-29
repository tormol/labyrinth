package tbm.util.collections;
import java.util.Objects;

public abstract class AbstractHasKey<K> implements HasKey<K> {
	protected final K key;
	protected AbstractHasKey(K key) {
		this.key = Objects.requireNonNull(key);
	}
	@Override//HasKey
	public final K getKey() {
		return key;
	}
	/**
	 *@return key.hashCode()*/@Override//Object
	public final int hashCode() {
		return key.hashCode();
	}
	@Override//Object
	public abstract boolean equals(Object o);
	@Override//Object
	public String toString() {
		return key.toString();
	}
}
