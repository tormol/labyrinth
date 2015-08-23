package tbm.util.collections;
import static java.util.Objects.requireNonNull;
import java.util.NoSuchElementException;
import tbm.util.collections.randomAccessIterators.UnmodifiableListIterator;

/**Iterate over a String, autoBoxing slows it down, but is avoidable with the _c methods
 *@see java.io.StringReader
 *@author tbm
 */
public class StringIterator extends UnmodifiableListIterator<Character> {
	protected final String str;

	public StringIterator(String str, int startAt) {
		super(startAt);
		this.str = requireNonNull(str);
	}

	public StringIterator(String str) {
		this(str, 0);
	}


	@Override protected Character getIndex(int index) {
		return str.charAt(index);
	}

	@Override protected int maxIndex() {
		return str.length();
	}


	/**get index of the next character to be returned*/
	public int getPosition() {
		return pos;
	}

	/**get the String this iterator is based on*/
	public String getString() {
		return str;
	}


	protected char get_c(int index, boolean update_pos) {
		try {
			char c = str.charAt(index);
			pos = index;
			return c;
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException();
		}
	}

	public char next_c() {
		char c = get_c(nextIndex(), true);
		forward = true;
		return c;
	}
	public char previous_c() {
		char c = get_c(previousIndex(), true);
		forward = false;
		return c;
	}

	public char peekNext_c() {
		return get_c(nextIndex(), false);
	}
	public char peekPrevious_c() {
		return get_c(previousIndex(), false);
	}
}
