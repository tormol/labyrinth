package tbm.util;

/**An implementation of a part of a r
 * 
 * 
 */
public class RegexClass {
	public static class InvalidRegexClassException extends IllegalArgumentException {
		protected InvalidRegexClassException(String str) {
			super(str);
		}
		private static final long serialVersionUID = 1L;
	}

	protected final char[] chars;
	protected final short ranges;
	protected final boolean inverted;

	public static RegexClass compile(String regexClass) {
		return new RegexClass(regexClass);
	}
	protected RegexClass(String rc) {
		inverted  =  (rc.length() > 0  &&  rc.charAt(0) == '^');

		int ranges = 0;
		for (int i=inverted?2:1;  i+1 < rc.length();  i++)
			if (rc.charAt(i) == '-') {
				ranges++;
				i += 2;
			}
		if (ranges > Short.MAX_VALUE)
			throw new InvalidRegexClassException(rc);//obviously crazy
		this.ranges = (short)ranges;
		this.chars = new char[rc.length() - ranges - (inverted?1:0)];

		ranges = 0;
		int singles = 0;
		for (int i = inverted?1:0;  i < rc.length();  i++)
			if (i+2 < rc.length()  &&  rc.charAt(i+1) == '-') {
				if (rc.charAt(i) >= rc.charAt(i+2))
					throw new InvalidRegexClassException("Invalid range: "+rc.charAt(i)+" >= "+rc.charAt(i+2)+" in regexClass "+rc);
				chars[ranges++] = rc.charAt(i);
				i += 2;
				chars[ranges++] = rc.charAt(i);
			} else {
				chars[this.ranges*2 + singles] = rc.charAt(i);
				singles++;
			}
	}

	@Override//Object
	public String toString() {
		StringBuilder sb = new StringBuilder(ranges*2 + chars.length + (inverted?1:0));
		if (inverted)
			sb.append('^');
		int i = 0;
		while (i < ranges*2)
			sb.append(chars[i++]).append('-').append(chars[i++]);
		sb.append(chars, i, chars.length-i);
		return sb.toString();
	}


	/**Does c match this RegexClass?*/
	public boolean matches(char c) {
		for (int i=0; i<ranges*2; i++)
			if (c >= chars[i++]  &&  c <= chars[i])
				return !inverted;
		for (int i=ranges*2; i<chars.length; i++)
			if (c == chars[i])
				return !inverted;
		return inverted;
	}

	/**Does all characters match this RegexClass?*/
	public boolean matches(CharSequence str) {
		for (int i=0; i<str.length(); i++)
			if ( !matches(str.charAt(i)))
				return false;
		return true;
	}

	/**Does all characters match this RegexClass?*/
	public boolean matches(char... list) {
		for (char c : list)
			if ( !matches(c))
				return false;
		return true;
	}


	/**Does c match this RegexClass?*/
	public static boolean matches(String regexClass, char c) {
		boolean matchVal = true;
		int i = 0;
		if ( !regexClass.isEmpty()) {
			if (regexClass.charAt(0) == '^') {
				matchVal = false;
				i++;
			}
			while (i < regexClass.length())
				if (c == regexClass.charAt(i))
					return matchVal;
				else if (i+2 >= regexClass.length()  ||  regexClass.charAt(i+1) != '-')
					i++;
				else if (c > regexClass.charAt(i)  &&  c <= regexClass.charAt(i+2))
					return matchVal;
				else
					i += 3;
		}
		return !matchVal;
	}

	public static boolean matches(String regexClass, CharSequence str) {
		for (int i=0; i<str.length(); i++)
			if ( !matches(regexClass, str.charAt(i)))
				return false;
		return true;
	}

	/**Does all characters match this RegexClass?*/
	public static boolean matches(String regexClass, char... list) {
		for (int i=0; i<list.length; i++)
			if ( !matches(regexClass, list[i]))
				return false;
		return true;
	}
}
