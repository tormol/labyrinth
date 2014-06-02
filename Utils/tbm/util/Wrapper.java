package tbm.util;

import java.io.Serializable;

public class Wrapper<T> implements Serializable, Cloneable {
	public T v;
	public Wrapper()
		{}
	public Wrapper(T v) {
		this.v = v;
	}
	public T get() {
		return v;
	}
	public void set(T v) {
		this.v = v;
	}
	@Override//Cloneable
	public Wrapper<T> clone() {
		return new Wrapper<T>(v);
	}
	private static final long serialVersionUID = 1L;
}
