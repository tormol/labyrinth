package tbm.util.collections;
import java.util.Iterator;
import java.util.Objects;
import tbm.util.CharSupplier;
import tbm.util.WillNeverThrowException;

public class StringIterator implements Iterator<Character>, CharSupplier<WillNeverThrowException> {
	protected final String str;
	protected final int length;//would be padded anyway on 64bit, should 
	protected int pos=0;
	public StringIterator(String str) {
		this.str = Objects.requireNonNull(str);
		this.length = str.length();
	}
	@Override
	public boolean hasNext() {
		return pos != length;
	}
	/**@deprecated you want to use {@code fetch()} when you're not calling trough an Iterator.
	  *@return null if at the end of the String.
	  */@Deprecated @Override//Iterator
	public Character next() {
		if (pos == length)//would catching IndexOutOfBoundsException be faster?
			return null;
		char ch = str.charAt(pos);
		pos++;
		return ch;
	}@Override//CharSupplier
	public int fetch() {
		if (pos == length)
			return -1;
		int ch = str.charAt(pos);
		pos++;
		return ch;
	}/**total length of the string.*/
	public int length() {
		return length;
	}/**current position*/
	public int position() {
		return pos;
	}
	public String getString() {
		return str;
	}
}
