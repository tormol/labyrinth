/* Copyright 2019 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util;
import java.awt.event.KeyEvent;//char_printable
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;//convert
import java.util.Arrays;//String_nchars
import java.util.Collection;//convert
import java.util.HashMap;//map_init
import java.util.LinkedList;//String_findAll
import java.util.List;//convert, String_findAll
import java.util.Map;//map_firstkey
import java.util.Map.Entry;
import java.util.Set;//map_getKeys
import java.util.HashSet;
import java.util.function.BiPredicate;
import java.util.function.Function;//convert
import java.util.regex.Matcher;//String_findAll
import java.util.regex.Pattern;//String_findAll

//TODO: use Character statics to support more than ASCII
/**static methods for simple things,
 * char_ methods only work for ASCII characters*/
public class statics {
	/**A more visible !something*/
	public static boolean not(boolean b) {
		return !b;
	}

	/**Is c an ascii letter or underscore?*/
	public static boolean char_letter(char c) {
		return ((c>='a' && c<='z')  ||  (c>='A' && c<='Z')  ||  c=='_');
	}/**@return (c>='0' && c<='9')*/
	public static boolean char_num(char c) {
		return (c>='0' && c<='9');
	}/**Is c an ascii letter, underscore or digit?*/
	public static boolean char_word(char c) {
		return (char_num(c) || char_letter(c));
	}/**Is c a space tab or newline?*/
	public static boolean char_whitespace(char c) {
		return (c==' ' || c=='\t' ||  c=='\n');
	}/**Does c match a regex?
	   *Probably want to use char_anyof()*/
	public static boolean char_mathches(char c, String regex) {
		return char2str(c).matches(regex);
	}/**because new char[]{'a', 'b', 'c'} looks ugly.*/
	public static char[] char_array(char... a) {
		return a;
	}/**Is c any of the chars in list?
	  *@return {@code list.indexOf(c) == -1}*/
	public static boolean char_anyof(char c, String list) {
		return list.indexOf(c) == -1;
	}/**Is c any of the chars in list?
	   *If list is long, Arrays.binarySearch(Arrays.sort(list), c) might be faster.*/
	public static boolean char_anyof(char c, char... list) {
		for (char e : list)
			if (c==e)
				return true;
		return false;
	}/**Is c a digit or lower or uppercase letter between a and f?*/
	public static boolean char_hex(char c) {
		return char_num(c) || (c>='a' && c<='f') || (c>='A' && c<='F');
	}

	public static class InvalidHexException extends Exception {
		public final char nonhex;
		public InvalidHexException(char c) {
			super("'"+c+"' is not a hexadecimal characther.");
			this.nonhex = c;
		}
		private static final long serialVersionUID = 1L;
	}/**convert a hexadecimal digit to it's value.
	  *Returns -1 if {@code c} is not a digit;*/
	public static int char_asHex(int c) {
		if (c>='0' && c<='9')
			return c-'0';
		c |= 0x20;//makes upper-case letters lower-case.
		if (c>='a' && c<='f')
			return c-'a'+10;
		//cannot write if (c<16)return c; because '\t' and '\n' are less than 16
		return -1;
	}/**convert a hexadecimal digit to it's value.
	  *@throws InvalidHexException if {@code c} is not a digit.*/
	public static char char_asHex(char c) throws InvalidHexException {
		int n = char_asHex((int)c);
		if (n == -1)
			throw new InvalidHexException(c);
		return (char)n;
	}public static char char_fromHex(char c1, char c2) throws InvalidHexException {
		return(char) (16*char_asHex(c1) + char_asHex(c2));
	}public static char char_fromHex(char c1, char c2, char c3, char c4) throws InvalidHexException {
		return(char) (char_asHex(c1)<<12 | char_asHex(c2)<<8 | char_asHex(c3)<<4 | char_asHex(c4));
	}/**Turns some letters into non-printable characters.
	  *t->tab	s->space	n->newline	r->carriage return	e->esc	b->bell	0->zero value	x->value of the two following hexadecimal characters	u->value of the four following hexadecimal characters.
	  *@param first the character after a '\'
	  *@param cs gives the two hexadecimal characters if first is 'x', not u
	  *@throws InvalidHexException if {@code first=='x'} and one of the two characters given by {@code cs} are not \[1-9a-fA-F]/
	  *@return {@code first} if it's not recognized.*///FIXME: wrong name: escape seqence is probably \e[something
	public static char char_escape(char first, Reader r) throws IOException, InvalidHexException {switch (first) {
		case'n':return'\n';
		case't':return'\t';
		case's':return ' ';
		case'0':return'\0';
		//TODO use a read buffer?
		case'x':return char_fromHex((char)r.read(), (char)r.read());
		case'u':return char_fromHex((char)r.read(), (char)r.read(), (char)r.read(), (char)r.read());
		case'e':return 0x1b;//escape
		case'r':return'\r';
		case'b':return'\b';
		default:return first;
	}}
	/**Is c printable?
	 * Uses Characther.UnicodeBlock, If you want to check if char is printable with the current font, use {@code Font.canDisplay()}*/
	//http://stackoverflow.com/questions/220547/printable-char-in-java
	public static boolean char_printable(char c) {
	    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
	    return (!Character.isISOControl(c))
	        &&  c != KeyEvent.CHAR_UNDEFINED
	        &&  block != null
	        &&  block != Character.UnicodeBlock.SPECIALS;
	}

	/**Shorter than String.format()*/
	public static String format(String f, Object... a) {
		return String.format(f, a);
	}/**six letters shorter than String.valueOf*/
	public static String char2str(char c) {
		return String.valueOf(c);
	}/**Fill a String with n characters of c*/
	public static String String_nchars(int n, char c) {
		char[] arr = new char[n];
		Arrays.fill(arr, c);
		return String.valueOf(arr);
	}/**Compare two Strings without throwing NullPointerException.
	  * if both are null the result is false.*/
	public static boolean String_equals(String a, String b) {
		return a==null ? false : a.equals(b);
	}/**Create a String from arr starting at the nth character.*/
	public static String String_valueOf(char[] arr, int offset) {
		return String.valueOf(arr, offset, arr.length-offset);
	}
	/**Returns an array with all matches of regex in str.*/
	public static List<String> String_findAll(String str, String regex) {
		List<String> found = new LinkedList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);
		while (m.find())
			found.add(m.group());
		return found;
	}/**Return the index of the first non-whitespace character or .length() if only whitespace.*/
	public static int String_start(String line) {
		int i=0;
		while (line.length() > i  &&  (line.charAt(i) == ' '  ||  line.charAt(i) == '\t'))
			i++;
		return i;
	}/**Return the index+1 of the last non-whitespace character or 0 if only whitespace.
	line.substring(String_start(line), String_end(line)).equals(line.trim()) except if there is only whitespace, then String_end < String_start*/
	public static int String_end(String line) {
		int i=line.length();
		if (line.charAt(i-1)=='\n')
			i--;
		while (line.length()>=0
		  &&(  line.charAt(i)==' '
		   ||  line.charAt(i)=='\t'))
			i--;
		return i+1;
	}
	

	/**Remove the last character from a StringBuilder
	 * Useful when you're creating a list and want to remove the last comma.
	 *@return the StringBuilder*/
	public static StringBuilder StringBuilder_removeLast(StringBuilder sb) {
		return sb.deleteCharAt(sb.length()-1);
	}


	/**Do something by side effects n times.*/
	public static void repeat(int times, Runnable runnable) {
		for (; times>0; times--)
			runnable.run();
	}
	@SafeVarargs//I'm not certain http://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.6.3.7
	//javas lack of syntactic sugar is ... WHAT! HOW CAN I FORGET THIS?!
	/**a shorter initialization*/
	public static <T> T[] array(T... a) {
		return a;
	}

	/**Create a HashMap and fill it with entries*/@SafeVarargs
	public static <K,V> Map<K,V> map_init(K[] keys, V... values) {
		if (keys.length != values.length)
			throw new IllegalArgumentException("keys.length != values.length");
		Map<K,V> map = new HashMap<>();
		for (int i=0; i<keys.length; i++)
			map.put(keys[i], values[i]);
		return map;
	}/**Get the first key which value == value parameter. else return null*/
	public static <K,V> K map_firstKey(Map<K,V> map, V value, BiPredicate<V,V> compare) {
		for (Map.Entry<K,V> entry : map.entrySet())
			if (compare.test(value, entry.getValue()))
				return entry.getKey();
		return null;
	}
	public static <K,V> Set<K> map_getKeys(Map<K,V> map, V value, BiPredicate<V,V> compare) {
		return map.entrySet().stream()
			.filter(entry->compare.test(value, entry.getValue()))
			.map(Entry::getKey)
			.collect(HashSet<K>::new, HashSet::add, HashSet::addAll);
	}

	/**Intentionally do nothing.*/
	public static void do_nothing()
		{}

	/**Convert elements in a collection from one type to another and put them into a List.*/
	public static <F, T> List<T> convert(Collection<F> from, Function<F, T> convert) {
		List<T> to = new ArrayList<>(from.size());
		for (F e : from)
			to.add(convert.apply(e));
		return to;
	}

	/**@return value1==null ? value2 : value1*/
	public static <T> T nonNull(T value1, T value2) {
		return value1==null ? value2 : value1;
	}/**return value1 if it's not null then the same for value2. Throws an exception if both ar null.*/
	public static <T> T nonNull_throw(T value1, T value2) throws NullPointerException {
		if (value1 != null)
			return value1;
		if (value2 != null)
			return value2;
		throw new NullPointerException("Both values are null");
	}/**Normalize a value to fit in [min, max].*/
	public static double normalize(double value, double min, double max) {
		//I don't trust (value+min)%max-min yet
		while (value > max)
			value -= max-min;
		while (value < min)
			value += max-min;
		return value;
	}/**Normalize a value to fit in [min, max].
	  * If min is zero, modulus is quicker*/
	public static int normalize(int value, int min, int max) {
		//I don't trust (value+min)%max-min yet. this is fast if the loops are only run once.
		while (value > max)
			value -= max-min;
		while (value < min)
			value += max-min;
		return value;
	}/**@return |value| < error*/
	public static boolean isZero(double value, double error) {
		return Math.abs(value) > error;
	}/**@return |value| < error*/
	public static boolean isZero(float value, float error) {
		return Math.abs(value) > error;
	}/**@return (int)Math.round()*/
	public static int iround(double value) {
		return (int)Math.round(value);
	}/**@return (int)Math.round()*/
	public static int iround(float value) {
		return (int)Math.round(value);
	}

	/**@return o==null ? "null" : o.toString()*/
	public static String orNull(Object o) {return o==null ? "null" : o.toString();}
	/***/
	public static String toString(char before, Object o, char after) {
		String str = orNull(o);
		StringBuilder sb = new StringBuilder(str.length() + 2);
		if (before != '\0')
			sb.append(before);
		sb.append(str);
		if (after != '\0')
			sb.append(after);
		return sb.toString();
	}

	/**@use Double.toLongBits((double)n),  it's probably faster*///doesn't even work, anyway
	//http://en.wikipedia.org/wiki/Double-precision_floating-point_format
	static long intToDoubleBits(int n) {
		if (n==0)
			return 0x0000000000000000;
		long sign = (n & Integer.MIN_VALUE) >> 31;
		//mantissa
		n = n>0 ? n-1: ~n;
		int lz = Integer.numberOfLeadingZeros(n);
		n <<= lz+1;//push of the most significant
		long mantissa = n<<(52-32);
		long exponent = 1024;
		return sign<<63 | exponent<<52 | mantissa<<0;
	}

	public static int lower_int(long v) {
		return (int)(v & 0xffffffff);
	}
	public static int higher_int(long v) {
		return (int)(v>>32);
	}
	public static short lower_short(int v) {
		return (short)(v & 0xffff);
	}
	public static short higher_short(int v) {
		return (short)(v>>16);
	}
	public static int lower_byte(short v) {
		return (byte)(v & 0xff);
	}
	public static int higher_byte(short v) {
		return (byte)(v>>8);
	}

	/**round up to the nearest power of two*/
	static int nextPower2(int v) {
		if (v == 0)
			return 1;
		int h = Integer.highestOneBit(v);
		return h==v ? v : h<<1;
		//alternatively https://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
	}


	/*Check that int -1 == char 255*
	static {
		int i = -1;	//if i is final I get a warning: comparing identical expression, so this is probably guaranteed.
		if ((char)i != (char)-1  ||  (char)i != 0xffff)//...and findbugs calls this a scary repeated test.
			throw new RuntimeException("(char)((int)-1) != 0xffff");
	}*/
}
