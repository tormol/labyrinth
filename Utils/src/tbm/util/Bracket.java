package tbm.util;

/**An enum for matching <>()[]{] pairs.
 * Quote marks are used for different things, and dosn't belong here.
 */
public enum Bracket {
	PAREN('(',')'), SQUARE('[',']'), CURLY('{','}'), ANGLE('<','>');
	public final char open, close;
	private Bracket(char o, char c) {
		open=o;
		close=c;
	}

	/**@return null if doesn't contain .close or everything until .close*/
	public String subString(String str) {
		int index = str.indexOf(close);
		if (index == -1)
			return null;
		return str.substring(0, index);
	}


	/**return the Bracket with that opener, or null if none have it.*/
	public static Bracket get(char opener) {switch (opener) {
		case'(': return PAREN;
		case'{': return CURLY;
		case'[': return SQUARE;
		case'<': return ANGLE;
		default: return null;
	}}

	/**return the Bracket with that closer, or null if none have it.*/
	public static Bracket get_by_closer(char closer) {switch (closer) {
		case')': return PAREN;
		case'}': return CURLY;
		case']': return SQUARE;
		case'>': return ANGLE;
		default: return null; 
	}}

	/***/
	public static Bracket find(char c) throws RuntimeException {
		Bracket b = get(c);
		if (b != null)
			return b;
		b = get_by_closer(c);
		if (b != null)
			return b;
		throw new RuntimeException('\''+c+"' is not a part of a pair.");
	}

	/**return the corresponding character, or throw an Exception*/
	public static char closes(char opener) throws RuntimeException {
		try {
			return get(opener).close;
		} catch (NullPointerException e) {
			throw new RuntimeException('\''+opener+"' doesn't open any pair.");
		}
	}

	/**return the corresponding character, or throw an Exception*/
	public static char opens(char closer) throws RuntimeException {
		try {
			return get(closer).open;
		} catch (NullPointerException e) {
			throw new RuntimeException('\''+closer+"' doesn't close any pair.");
		}
	}

	/**return the corresponding character, or throw an Exception*/
	public static char matching(char c) throws RuntimeException {
		Bracket b = get(c);
		if (b != null)
			return b.close;
		b = get_by_closer(c);
		if (b != null)
			return b.open;
		throw new RuntimeException('\''+c+"' is not a part of a pair.");
	}
}
