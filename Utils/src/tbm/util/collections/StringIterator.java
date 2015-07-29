package tbm.util.collections;
import java.util.Iterator;
import java.util.Objects;

/**Iterate over a String, autoBoxing slows it down, but if you use hasNext() and next(_c() you can avoid this
 * @see java.io.StringReader
 * @author tbm
 */
public class StringIterator implements Iterator<Character>, Iterable<Character> {
	protected final String str;
	protected final int length;//would be padded anyway on 64bit, should make hasNext() faster 
	protected int pos=0;
	public StringIterator(String str) {
		this.str = Objects.requireNonNull(str);
		this.length = str.length();
	}
	@Override//Iterator
	public boolean hasNext() {
		return pos != length;
	}
	/**@deprecated you want to use {@code next_c()} or {@code read()} when you're not calling trough an Iterator.
	  *@return null if at the end of the String.
	  */@Deprecated @Override//Iterator
	public Character next() {
		//catching IndexOutOfBoundsException would be slightly slower until the end where it's a lot slower.
		//exceptions should only be used for exceptional situations
		return hasNext() ? str.charAt(pos++) : null;
	}/**{@code next()} without boxing/unboxing
	  *Imitates Reader, but without IOException
	  *@return the next character or -1 if at the end.*/
	public int read() {
		return hasNext() ? str.charAt(pos++) : -1;
	}/**{@code next()} without boxing/unboxing
	  *@throws IndexOutOfBoundsException if at the end, so check hasNext() first!*/
	public char next_c() throws IndexOutOfBoundsException {
		char c = str.charAt(pos);
		pos++;//be sure charAt throws before incrementing
		return c;
	}/**get index of the next character to be returned*/
	public int getPosition() {
		return pos;
	}/**get the String this iterator is based on*/
	public String getString() {
		return str;
	}
	/***Allows use with for loop.
	 *@deprecated should only be used by for loops.
	 *@return {@code this}*/@Deprecated @Override//Iterable
	public Iterator<Character> iterator() {
		return this;
	}
}
