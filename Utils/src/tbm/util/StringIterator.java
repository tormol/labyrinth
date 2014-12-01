package tbm.util;
import java.util.Iterator;

public class StringIterator implements Iterator<Character>, CharSupplier<WillNeverThrowException> {
	protected final String str;
	protected final int length;
	protected int pos=0;
	public StringIterator(String str) {
		if (str==null)
			throw new IllegalArgumentException("Cannot be null.");
		this.str = str;
		this.length = str.length();
	}
	@Override
	public boolean hasNext() {
		return pos==length;
	}
	@Override@Deprecated
	/**@deprecated you want to use next_ch() when you're not calling trough an Iterator.
	  *@return null if at the end of the String.*/
	public Character next() {
		if (pos == length)
			return null;
		char ch = str.charAt(pos);
		pos++;
		return ch;
	}/***/
	public char next_ch() {
		if (pos == length)
			return '\0';
		char ch = str.charAt(pos);
		pos++;
		return ch;
	}/**the total length of the string.*/
	public int length() {
		return length;
	}
	public int position() {
		return pos;
	}
	public String getString() {
		return str;
	}
	@Override
	public int get() {
		return next_ch();
	}
}
