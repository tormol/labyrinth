package tbm.util;
import java.util.Objects;

/**For methods that read data character by character and wants to pass through checked exceptions like IOExceptions.*/
public interface CharSupplier<EX extends Throwable> {
	/**get the next character
	 * When there are no more characters, returns -1 or throws EX.
	 *@throws EX if an error occurs before hitting the end.*/
	int fetch() throws EX;

	/**Creates a CharSupplier from a String
	 *Will never throw */
	public static CharSupplier<WillNeverThrowException> fromString(String string) {
		return new CharSupplier<WillNeverThrowException>() {
			public final String str = Objects.requireNonNull(string);
			private int pos = -1;
			public int fetch() throws WillNeverThrowException {
				pos++;
				return pos < str.length()  ?  str.charAt(pos)  :  -1;
			}
		};
	}
}
